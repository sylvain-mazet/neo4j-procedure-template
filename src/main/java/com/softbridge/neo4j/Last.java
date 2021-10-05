package com.softbridge.neo4j;

import org.neo4j.procedure.*;

/**
 * This is an example how you can create a simple user-defined function for Neo4j.
 */
public class Last {

    @UserAggregationFunction
    @Description("com.softbridge.neo4j.last(value) - returns last non-null row")
    public LastFunction last() {
        return new LastFunction();
    }


    public static class LastFunction {

        private Object lastValue;

        @UserAggregationUpdate
        public void aggregate(@Name("value") Object value) {
            if (value != null) {
                this.lastValue = value;
            }
        }

        @UserAggregationResult
        public Object result() {
            return lastValue;
        }
    }
}
