// Build using https://github.com/jACT-RTeam/jactr-workflow-libs

import org.jactr.Config
import org.jactr.ConfigBuilder
import org.jactr.Build

def config = new ConfigBuilder('http://monochromata.de/maven/releases/org.commonreality/org/commonreality/core/maven-metadata.xml',
						'https://github.com/monochromata/commonreality.git')
				.propertyForEclipseVersion('commonreality.eclipse.version')
				.build()
new Build().run(config)