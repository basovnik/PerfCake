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
package org.perfcake.reporting.reporters.accumulators;

/**
 * Accumulates the sum of values.
 * Atomic types are not used because both values must be set at the same time. Hence the methods are synchronized.
 * 
 * @author Pavel Macík <pavel.macik@gmail.com>
 * 
 */
public class SumAccumulator implements Accumulator<Double> {
   /**
    * Sum of the reported values
    */
   private double sum = 0d;

   /*
    * (non-Javadoc)
    * 
    * @see org.perfcake.reporting.reporters.accumulators.Accumulator#add(java.lang.Object)
    */
   @Override
   public synchronized void add(final Double number) {
      sum = sum + number;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.perfcake.reporting.reporters.accumulators.Accumulator#getResult()
    */
   @Override
   public synchronized Double getResult() {
      return sum;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.perfcake.reporting.reporters.accumulators.Accumulator#reset()
    */
   @Override
   public synchronized void reset() {
      sum = 0d;
   }

}
