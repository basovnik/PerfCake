/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.perfcake.parsing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.perfcake.PerfCakeException;
import org.perfcake.RunInfo;
import org.perfcake.common.BoundPeriod;
import org.perfcake.common.Period;
import org.perfcake.common.PeriodType;
import org.perfcake.message.Message;
import org.perfcake.message.MessageTemplate;
import org.perfcake.message.generator.AbstractMessageGenerator;
import org.perfcake.message.generator.LongtermMessageGenerator;
import org.perfcake.message.sender.MessageSenderManager;
import org.perfcake.parser.ScenarioParser;
import org.perfcake.reporting.ReportManager;
import org.perfcake.reporting.destinations.Destination;
import org.perfcake.reporting.destinations.DummyDestination;
import org.perfcake.reporting.reporters.DummyReporter;
import org.perfcake.reporting.reporters.Reporter;
import org.perfcake.reporting.reporters.WarmUpReporter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ScenarioParserTest {
   private ScenarioParser scenarioParser, noValidationScenarioParser, noMessagesScenarioParser;

   private static final int THREADS = 10;
   private static final String MESSAGE1_CONTENT = "Stupid is as supid does! :)";
   private static final String MESSAGE2_CONTENT = "I'm the fish!";
   private static final String SENDER_CLASS = "org.perfcake.message.sender.HTTPSender";
   private static final String FISH_VALIDATOR_ID = "fishValidator";
   private static final String SMILE_VALIDATOR_ID = "smileValidator";
   private static final String STUPID_VALIDATOR_ID = "stupidValidator";
   private static final String FILTERED_PROPERTY_VALUE = "filtered-property-value";
   private static final String DEFAULT_PROPERTY_VALUE = "default-property-value";

   @BeforeClass
   public void prepareScenarioParser() throws PerfCakeException, URISyntaxException, IOException {
      System.setProperty("perfcake.messages.dir", getClass().getResource("/messages").getPath());
      System.setProperty("test.filtered.property", FILTERED_PROPERTY_VALUE);
      scenarioParser = new ScenarioParser(getClass().getResource("/scenarios/test-scenario.xml"));
      noValidationScenarioParser = new ScenarioParser(getClass().getResource("/scenarios/test-scenario-no-validation.xml"));
      noMessagesScenarioParser = new ScenarioParser(getClass().getResource("/scenarios/test-scenario-no-messages.xml"));
   }

   @Test
   public void parseScenarioPropertiesTest() {
      try {
         Properties scenarioProperties = scenarioParser.parseScenarioProperties();
         Assert.assertEquals(scenarioProperties.get("quickstartName"), "testQS", "quickstartName property");
         Assert.assertEquals(scenarioProperties.get("filteredProperty"), FILTERED_PROPERTY_VALUE, "filteredProperty property");
         Assert.assertEquals(scenarioProperties.get("defaultProperty"), DEFAULT_PROPERTY_VALUE, "defaultProperty property");
      } catch (PerfCakeException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
   }

   @Test
   public void parseSenderTest() {
      try {
         MessageSenderManager senderManager = scenarioParser.parseSender(THREADS);
         Assert.assertEquals(senderManager.getSenderClass(), SENDER_CLASS, "senderClass");
         Assert.assertEquals(senderManager.getSenderPoolSize(), THREADS, "senderPoolSize");
         // TODO: add assertions on a sender
      } catch (PerfCakeException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
   }

   @Test
   public void parseGeneratorTest() {
      try {
         AbstractMessageGenerator generator = scenarioParser.parseGenerator();
         Assert.assertTrue(generator instanceof LongtermMessageGenerator, "The generator is not an instance of " + LongtermMessageGenerator.class.getName());
         LongtermMessageGenerator lmg = (LongtermMessageGenerator) generator;
         lmg.setRunInfo(new RunInfo(new Period(PeriodType.TIME, 30L)));
         Assert.assertEquals(lmg.getThreads(), THREADS, "threads");
         Assert.assertEquals(lmg.getThreadQueueSize(), 5000);
      } catch (PerfCakeException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
   }

   @Test
   public void parseMessagesTest() {
      try {
         // Message store
         List<MessageTemplate> messageStore = scenarioParser.parseMessages();
         Assert.assertEquals(messageStore.size(), 2);

         // Message 1
         MessageTemplate mts1 = messageStore.get(0);
         Assert.assertEquals(mts1.getMultiplicity(), new Long(10), "message1 multiplicity");
         Message m1 = mts1.getMessage();
         // Message 1 content
         Assert.assertEquals(m1.getPayload(), MESSAGE1_CONTENT, "message1 content");
         // Message 1 headers
         Properties headers1 = m1.getHeaders();
         Assert.assertEquals(headers1.size(), 3, "message1 headers count");
         Assert.assertEquals(headers1.get("m_header1"), "m_h_value1", "message1 header1");
         Assert.assertEquals(headers1.get("m_header2"), "m_h_value2", "message1 header2");
         Assert.assertEquals(headers1.get("m_header3"), "m_h_value3", "message1 header3");
         // Message 1 properties
         Properties properties1 = m1.getProperties();
         Assert.assertEquals(properties1.size(), 3, "message1 properties count");
         Assert.assertEquals(properties1.get("m_property1"), "m_p_value1", "message1 property1");
         Assert.assertEquals(properties1.get("m_property2"), "m_p_value2", "message1 property2");
         Assert.assertEquals(properties1.get("m_property3"), "m_p_value3", "message1 property3");
         // Message 1 validatorIds
         List<String> validatorIdList1 = mts1.getValidatorIdList();
         Assert.assertEquals(validatorIdList1.size(), 2, "message1 validatorIdList size");
         Assert.assertEquals(validatorIdList1.get(0), STUPID_VALIDATOR_ID, "message1 stupidValidatorId");
         Assert.assertEquals(validatorIdList1.get(1), SMILE_VALIDATOR_ID, "message1 smileValidatorId");

         // Message 2
         MessageTemplate mts2 = messageStore.get(1);
         Assert.assertEquals(mts2.getMultiplicity(), new Long(1), "message2 multiplicity");
         Message m2 = mts2.getMessage();
         // Message 2 content
         Assert.assertEquals(m2.getPayload(), MESSAGE2_CONTENT, "message2 content");
         // Message 2 headers
         Properties headers2 = m2.getHeaders();
         Assert.assertEquals(headers2.size(), 0, "message2 headers count");
         // Message 2 properties
         Properties properties2 = m2.getProperties();
         Assert.assertEquals(properties2.size(), 0, "message2 properties count");
         // Message 2 validatorIds
         List<String> validatorIdList2 = mts2.getValidatorIdList();
         Assert.assertEquals(validatorIdList2.size(), 1, "message2 validatorIdList size");
         Assert.assertEquals(validatorIdList2.get(0), FISH_VALIDATOR_ID, "message2 fishValidatorId");

         // Messages section is optional
         List<MessageTemplate> emptyMessageStore = noMessagesScenarioParser.parseMessages();
         Assert.assertTrue(emptyMessageStore.isEmpty(), "empty message store with no messages in scenario");

      } catch (PerfCakeException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
   }

   @Test
   public void parseReportingTest() {
      try {
         ReportManager reportManager = scenarioParser.parseReporting();
         Assert.assertNotNull(reportManager);
         Assert.assertEquals(reportManager.getReporters().size(), 2, "reportManager's number of reporters");
         final String DUMMY_REPORTER_KEY = "dummy";
         final String WARM_UP_REPORTER_KEY = "warmup";

         Map<String, Reporter> reportersMap = new HashMap<>();
         for (Reporter reporter : reportManager.getReporters()) {
            if (reporter instanceof DummyReporter) {
               reportersMap.put(DUMMY_REPORTER_KEY, reporter);
            } else if (reporter instanceof WarmUpReporter) {
               reportersMap.put(WARM_UP_REPORTER_KEY, reporter);
            } else {
               Assert.fail("The reporter should be an instance of either " + DummyReporter.class.getCanonicalName() + " or " + WarmUpReporter.class.getCanonicalName() + ", but it is an instance of " + reporter.getClass().getCanonicalName() + "!");
            }
         }

         Reporter reporter = reportersMap.get(DUMMY_REPORTER_KEY);
         Assert.assertEquals(reporter.getDestinations().size(), 1, "reporter's number of destinations");
         Destination destination = reporter.getDestinations().iterator().next();
         Assert.assertTrue(destination instanceof DummyDestination, "destination's class");
         Assert.assertEquals(((DummyDestination) destination).getProperty(), "dummy_p_value", "destination's property value");
         Assert.assertEquals(((DummyDestination) destination).getProperty2(), "dummy_p2_value", "destination's property2 value");
         int assertedPeriodCount = 0;
         Assert.assertEquals(reporter.getReportingPeriods().size(), 3);
         for (BoundPeriod<Destination> period : reporter.getReportingPeriods()) {
            switch (period.getPeriodType()) {
               case TIME:
                  Assert.assertEquals(period.getPeriod(), 2000, "time period's value");
                  assertedPeriodCount++;
                  break;
               case ITERATION:
                  Assert.assertEquals(period.getPeriod(), 100, "iteration period's value");
                  assertedPeriodCount++;
                  break;
               case PERCENTAGE:
                  Assert.assertEquals(period.getPeriod(), 50, "percentage period's value");
                  assertedPeriodCount++;
                  break;
            }
         }
         Assert.assertEquals(assertedPeriodCount, 3, "number of period asserted");

         Reporter warmUpReporter = reportersMap.get(WARM_UP_REPORTER_KEY);
         Assert.assertTrue(warmUpReporter instanceof WarmUpReporter, "reporter's class");
         Assert.assertEquals(warmUpReporter.getDestinations().size(), 0, "reporter's number of destinations");
         Assert.assertEquals(((WarmUpReporter) warmUpReporter).getMinimalWarmUpCount(), 12345, "reporter's minimal warmup count");
         Assert.assertEquals(((WarmUpReporter) warmUpReporter).getMinimalWarmUpDuration(), 15000, "reporter's minimal warmup duration");
         Assert.assertEquals(((WarmUpReporter) warmUpReporter).getAbsoluteThreshold(), 0.2d, "reporter's absolute threshold");
         Assert.assertEquals(((WarmUpReporter) warmUpReporter).getRelativeThreshold(), 1d, "reporter's relative threshold");

      } catch (PerfCakeException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
   }

   @Test
   public void parseValidationTest() {
      try {
         scenarioParser.parseValidation();
         // TODO: add assertions on validation

         // validation is optional
         noValidationScenarioParser.parseValidation();
      } catch (PerfCakeException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
   }
}