/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.state;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.RuleIdentifiers;
import org.apache.shardingsphere.infra.rule.identifier.type.resoure.ResourceHeldRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.cluster.ClusterStateContext;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverStateContextTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        TransactionRule transactionRule = mock(TransactionRule.class);
        ResourceHeldRule<ShardingSphereTransactionManagerEngine> resourceHeldRule = mock(ResourceHeldRule.class);
        when(resourceHeldRule.getResource()).thenReturn(mock(ShardingSphereTransactionManagerEngine.class));
        when(transactionRule.getRuleIdentifiers()).thenReturn(new RuleIdentifiers(resourceHeldRule));
        TrafficRule trafficRule = mock(TrafficRule.class);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Arrays.asList(transactionRule, trafficRule));
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                mock(MetaDataPersistService.class), new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), globalRuleMetaData, new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(contextManager.getInstanceContext().getInstance().getState()).thenReturn(new InstanceStateContext());
        when(contextManager.getClusterStateContext()).thenReturn(new ClusterStateContext());
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        result.put(DefaultDatabase.LOGIC_NAME, database);
        return result;
    }
    
    @Test
    void assertGetConnectionWithOkState() {
        Connection actual = DriverStateContext.getConnection(DefaultDatabase.LOGIC_NAME, contextManager);
        assertThat(actual, instanceOf(ShardingSphereConnection.class));
    }
}
