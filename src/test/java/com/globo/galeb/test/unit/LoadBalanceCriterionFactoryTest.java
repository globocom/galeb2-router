package com.globo.galeb.test.unit;

import static org.assertj.core.api.Assertions.*;
import static com.globo.galeb.criteria.LoadBalanceCriterionFactory.DEFAULT_LOADBALANCE;
import static com.globo.galeb.criteria.LoadBalanceCriterionFactory.CLASS_PACKAGE;
import static com.globo.galeb.criteria.LoadBalanceCriterionFactory.CLASS_SUFFIX;

import java.util.EnumSet;

import org.junit.Test;

import com.globo.galeb.core.Backend;
import com.globo.galeb.criteria.ICriterion;
import com.globo.galeb.criteria.LoadBalanceCriterionFactory;

public class LoadBalanceCriterionFactoryTest {

    enum SupportedLoadBalance {
        Random,
        IPHash,
        LeastConn,
        RoundRobin
    }

    private ICriterion<Backend> loadbalanceCriterionDefault = LoadBalanceCriterionFactory.create(DEFAULT_LOADBALANCE);

    @Test
    public void loadDefaultIfInvalid() {
        ICriterion<Backend> loadbalanceCriterion = LoadBalanceCriterionFactory.create("Invalid Class");

        assertThat(loadbalanceCriterion).hasSameClassAs(loadbalanceCriterionDefault);
    }

    @Test
    public void loadDefaultIfNull() {
        ICriterion<Backend> loadbalanceCriterion = LoadBalanceCriterionFactory.create(null);

        assertThat(loadbalanceCriterion).hasSameClassAs(loadbalanceCriterionDefault);
    }

    @Test
    public void loadDefaultIfBlank() {
        ICriterion<Backend> loadbalanceCriterion = LoadBalanceCriterionFactory.create("");

        assertThat(loadbalanceCriterion).hasSameClassAs(loadbalanceCriterionDefault);
    }

    @Test
    public void loadSupportedLoadBalances() {
        for (SupportedLoadBalance supportedLoadBalance : EnumSet.allOf(SupportedLoadBalance.class)) {
            String loadBalance = supportedLoadBalance.toString();
            String expectedClassName = CLASS_PACKAGE+loadBalance+CLASS_SUFFIX;

            ICriterion<Backend> loadbalanceCriterion = LoadBalanceCriterionFactory.create(loadBalance);
            String loadbalanceCriterionName = loadbalanceCriterion.getClass().getName();

            assertThat(loadbalanceCriterionName).as("Testing "+loadBalance).isEqualTo(expectedClassName);
        }
    }

}