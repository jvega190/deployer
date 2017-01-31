/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.deployer.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.deployer.api.DeploymentPipeline;
import org.craftercms.deployer.api.DeploymentProcessor;
import org.craftercms.deployer.api.exceptions.DeployerConfigurationException;
import org.craftercms.deployer.api.exceptions.DeployerException;
import org.craftercms.deployer.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static org.craftercms.deployer.impl.DeploymentConstants.PROCESSOR_NAME_CONFIG_KEY;

/**
 * Created by alfonsovasquez on 12/22/16.
 */
@Component("deploymentPipelineFactory")
public class DeploymentPipelineFactoryImpl implements DeploymentPipelineFactory {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentPipelineFactoryImpl.class);

    @Override
    public DeploymentPipeline getPipeline(HierarchicalConfiguration configuration, ConfigurableApplicationContext applicationContext,
                                          String pipelinePropertyName) throws DeployerException {
        List<HierarchicalConfiguration> processorConfigs = ConfigUtils.getRequiredConfigurationsAt(configuration, pipelinePropertyName);
        List<DeploymentProcessor> processors = new ArrayList<>();

        for (HierarchicalConfiguration processorConfig : processorConfigs) {
            String processorName = ConfigUtils.getRequiredStringProperty(processorConfig, PROCESSOR_NAME_CONFIG_KEY);

            logger.debug("Initializing pipeline processor '{}'", processorName);

            try {
                DeploymentProcessor processor = applicationContext.getBean(processorName, DeploymentProcessor.class);
                processor.configure(processorConfig);

                processors.add(processor);
            } catch (NoSuchBeanDefinitionException e) {
                throw new DeployerConfigurationException("No processor prototype bean found with name '" + processorName + "'", e);
            } catch (Exception e) {
                throw new DeployerConfigurationException("Failed to initialize pipeline processor '" + processorName + "'", e);
            }
        }

        return new DeploymentPipelineImpl(processors);
    }

}
