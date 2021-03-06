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
package org.perfcake.scenario;

import org.apache.log4j.Logger;
import org.perfcake.PerfCakeConst;
import org.perfcake.PerfCakeException;
import org.perfcake.RunInfo;
import org.perfcake.message.MessageTemplate;
import org.perfcake.message.generator.AbstractMessageGenerator;
import org.perfcake.message.sender.AbstractSender;
import org.perfcake.message.sender.MessageSenderManager;
import org.perfcake.reporting.ReportManager;
import org.perfcake.reporting.reporters.Reporter;
import org.perfcake.util.Utils;
import org.perfcake.validation.MessageValidator;
import org.perfcake.validation.ValidationManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating {@link org.perfcake.scenario.Scenario} instance, which can be run by {@link org.perfcake.ScenarioExecution}
 * <p/>
 * Uses fluent API to setup builder.
 *
 * @author Jiří Sedláček <jiri@sedlackovi.cz>
 */
public class ScenarioBuilder {

   public static final Logger log = Logger.getLogger(ScenarioBuilder.class);

   private RunInfo runInfo;
   private AbstractSender senderTemplate;
   private List<Reporter> reporters = new ArrayList<>();
   private List<MessageTemplate> messages = new ArrayList<>();
   private AbstractMessageGenerator generator;
   private ValidationManager validationManager = new ValidationManager();

   private ReportManager reportManager;

   private MessageSenderManager messageSenderManager;

   public ScenarioBuilder() throws PerfCakeException {

   }

   /**
    * Sets message generator which will be used for {@link org.perfcake.scenario.Scenario}
    *
    * @param any
    *       message generator
    * @return this
    */
   public ScenarioBuilder setGenerator(AbstractMessageGenerator g) {
      this.generator = g;
      return this;
   }

   /**
    * Sets {@link RunInfo} object, which will be used for {@link org.perfcake.scenario.Scenario}
    *
    * @param RunInfo
    * @return this
    */
   public ScenarioBuilder setRunInfo(RunInfo ri) {
      this.runInfo = ri;
      return this;
   }

   /**
    * Sets {@link AbstractSender} implementation object, which will be used as a template for preparing pool of senders
    *
    * @param AbstractSender
    *       implementation
    * @return this
    */
   public ScenarioBuilder setSender(AbstractSender s) {
      this.senderTemplate = s;
      return this;
   }

   /**
    * Adds a {@link Reporter}, which will be used in {@link org.perfcake.scenario.Scenario} for reporting results. More reporters can be added
    *
    * @param Reporter
    *       implementation
    * @return this
    */
   public ScenarioBuilder addReporter(Reporter r) {
      reporters.add(r);
      return this;
   }

   /**
    * Adds a {@link MessageTemplate}, which will be used in {@link org.perfcake.scenario.Scenario}
    *
    * @param MessageTemplate
    * @return this
    */
   public ScenarioBuilder addMessage(MessageTemplate message) {
      messages.add(message);
      return this;
   }

   /**
    * Put validator under the key validatorId
    *
    * @param validatorId
    * @param message
    *       validator
    * @return
    */
   public ScenarioBuilder putMessageValidator(String validatorId, MessageValidator mv) {
      validationManager.addValidator(validatorId, mv);
      validationManager.setEnabled(true);
      return this;
   }

   /**
    * Builds the usable {@link org.perfcake.scenario.Scenario} object, which can be then used for executing the scenario.
    *
    * @return
    * @throws IllegalStateException
    *       if {@link RunInfo} is not set
    * @throws IllegalStateException
    *       if some generator is not set
    * @throws IllegalStateException
    *       if some sender is not set or messageSenderManager was not loaded
    */
   public Scenario build() throws Exception {
      if (runInfo == null) {
         throw new IllegalStateException("RunInfo is not set");
      }
      if (generator == null) {
         throw new IllegalStateException("Generator is not set");
      }
      if (messageSenderManager == null && senderTemplate == null) {
         throw new IllegalStateException("Sender is not set");
      }

      Scenario sc = new Scenario();
      generator.setRunInfo(runInfo);
      sc.setGenerator(generator);

      if (messageSenderManager == null) {
         MessageSenderManager msm = new MessageSenderManager();
         msm.setSenderClass(senderTemplate.getClass().getName());
         msm.setSenderPoolSize(generator.getThreads());
/*         for (int i = 0; i < generator.getThreads(); i++) {
            AbstractSender newInstance = senderTemplate.getClass().newInstance();
            BeanUtils.copyProperties(newInstance, senderTemplate);
            newInstance.init();
            msm.addSenderInstance(newInstance);
         }*/
         sc.setMessageSenderManager(msm);
      } else {
         sc.setMessageSenderManager(messageSenderManager);
      }

      if (reportManager == null) { // if report parsed directly
         reportManager = new ReportManager();
      }

      for (Reporter r : reporters) {
         reportManager.registerReporter(r);
      }

      reportManager.setRunInfo(runInfo);
      sc.setReportManager(reportManager);

      sc.setMessageStore(messages);
      sc.setValidationManager(validationManager);

      return sc;
   }

}
