/* *********************************************************************** *
 * project: org.matsim.*
 * Controller
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package telematics;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.signals.SignalSystemsConfigGroup;
import org.matsim.contrib.signals.builder.Signals;
import org.matsim.contrib.signals.controller.laemmerFix.LaemmerConfigGroup;
import org.matsim.contrib.signals.controller.sylvia.SylviaConfigGroup;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsDataLoader;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;


/**
 * @author dgrether, dziemke, tthunig
 */
public class Controller {

	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig( args[0]) ;
		config.qsim().setUsingFastCapacityUpdate(false);
		// add config groups for adaptive signals
		ConfigUtils.addOrGetModule(config, LaemmerConfigGroup.class);
		ConfigUtils.addOrGetModule(config, SylviaConfigGroup.class);

		config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.kinematicWaves);

		Scenario scenario = ScenarioUtils.loadScenario( config ) ;
		
		SignalSystemsConfigGroup signalsConfigGroup = ConfigUtils.addOrGetModule(config,
				SignalSystemsConfigGroup.GROUP_NAME, SignalSystemsConfigGroup.class);
		
		if (signalsConfigGroup.isUseSignalSystems()) {
			scenario.addScenarioElement(SignalsData.ELEMENT_NAME,
					new SignalsDataLoader(config).loadSignalsData());
		}
		Controler c = new Controler(scenario);
		c.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		c.getConfig().controler().setCreateGraphs(false);
		// add the signals module
		new Signals.Configurator( c ) ;
		
		c.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addControlerListenerBinding().to(RouteTTObserver.class);
			}
		});
		c.run();
	}
}
