/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.microshed.intellij.wizard;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.TextFieldWithStoredHistory;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBUI;
import org.microshed.intellij.MicroProfileModuleBuilder;
import org.microshed.intellij.model.ModuleInitializationData;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The first step of the MicroProfile module wizard in which user can choose the JDK, MicroProfile version and the runtime server.
 *
 * @author Ehsan Zaery Moghaddam (zaerymoghaddam@gmail.com)
 */
public class MicroProfileStarterApiUrlStep extends ModuleWizardStep {

    private static final Logger LOG = Logger.getInstance("#org.microshed.intellij.wizard.MicroProfileStarterApiUrlStep");

    private final ModuleInitializationData moduleCreationData;

    private TextFieldWithStoredHistory customUrlTextField;
    private ComponentWithBrowseButton<TextFieldWithStoredHistory> customUrlBrowser;

    public MicroProfileStarterApiUrlStep(ModuleInitializationData moduleCreationData) {
        this.moduleCreationData = moduleCreationData;
    }

    @Override
    public JComponent getComponent() {
        return JBUI.Panels.simplePanel(0, 10).addToTop(createTopPanel());
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        GridBagLayout topPanelLayout = new GridBagLayout();
        topPanel.setLayout(topPanelLayout);

        GridBagConstraints topPanelLayoutConstraint = new GridBagConstraints();
        topPanelLayoutConstraint.fill = GridBagConstraints.HORIZONTAL;
        topPanelLayoutConstraint.insets = JBUI.insets(2);
        topPanelLayoutConstraint.weightx = 0.25;
        topPanelLayoutConstraint.gridx = 0;

        topPanelLayoutConstraint.gridy = 0;
        topPanel.add(new JLabel("Choose MicroProfile Starter service URL:"), topPanelLayoutConstraint);

        topPanelLayoutConstraint.gridy++;
        topPanel.add(new Box.Filler(JBUI.size(0, 4), JBUI.size(0, 4), JBUI.size(Short.MAX_VALUE, 4)), topPanelLayoutConstraint);

        //  The "Default" radio
        JBRadioButton defaultServiceUrlRadio = new JBRadioButton("Default: ", true);
        defaultServiceUrlRadio.addActionListener(e -> customUrlBrowser.setEnabled(false));
        HyperlinkLabel starterURL = new HyperlinkLabel(MicroProfileModuleBuilder.STARTER_REST_BASE_URL);
        starterURL.setHyperlinkTarget(MicroProfileModuleBuilder.STARTER_REST_BASE_URL);
        starterURL.addHyperlinkListener(e -> BrowserUtil.browse(e.getURL()));
        JPanel defaultUrlPanel = JBUI.Panels.simplePanel(10, 0)
                .addToLeft(defaultServiceUrlRadio)
                .addToCenter(starterURL);

        topPanelLayoutConstraint.gridy++;
        topPanel.add(defaultUrlPanel, topPanelLayoutConstraint);

        //  The "Custom" radio
        customUrlTextField = new TextFieldWithStoredHistory("");
        customUrlBrowser = new ComponentWithBrowseButton<>(customUrlTextField, e -> Messages.showErrorDialog("Hi", ""));
        customUrlBrowser.setEnabled(false);
        JBRadioButton customServiceUrlRadio = new JBRadioButton("Custom:");
        customServiceUrlRadio.addActionListener(e -> customUrlBrowser.setEnabled(true));

        JPanel customUrlPanel = JBUI.Panels.simplePanel(10, 0)
                .addToLeft(customServiceUrlRadio)
                .addToCenter(customUrlBrowser);
        topPanelLayoutConstraint.gridy++;
        topPanel.add(customUrlPanel, topPanelLayoutConstraint);

        ButtonGroup apiRadios = new ButtonGroup();
        apiRadios.add(defaultServiceUrlRadio);
        apiRadios.add(customServiceUrlRadio);

        return topPanel;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if(customUrlTextField.isEnabled()) {
            if (customUrlTextField.getText().trim().isEmpty()) {
                throw new ConfigurationException("The custom URL can't be empty");
            } else {
                //  Try to create a URL object from given address to ensure it's a valid URL
                try {
                    new URL(customUrlTextField.getText());
                } catch (MalformedURLException e) {
                    throw new ConfigurationException("Invalid custom MicroProfile Starter URL");
                }
            }
        }

        return true;
    }

    @Override
    public void updateDataModel() {
        if(customUrlTextField.isEnabled()) {
            try {
                moduleCreationData.setStarterUrl(new URL(customUrlTextField.getText()));
            } catch (MalformedURLException e) {
                //  This should never happen as the validate() method is always called before this method being called
                LOG.error("The value of [" + customUrlTextField.getText() + "] does not have valid URL format", e);
            }
        } else {
            try {
                moduleCreationData.setStarterUrl(new URL(MicroProfileModuleBuilder.STARTER_REST_BASE_URL));
            } catch (MalformedURLException e) {
                //  This should never happen as the default URL value defined as constant is always a valid URL
                LOG.error("The value of [" + MicroProfileModuleBuilder.STARTER_REST_BASE_URL + "] does not have valid URL format", e);
            }
        }
    }
}
