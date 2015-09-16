package at.bayava.cdiunitextension

import org.jboss.weld.bootstrap.api.Bootstrap
import org.jboss.weld.bootstrap.api.CDI11Bootstrap
import org.jboss.weld.bootstrap.spi.Deployment
import org.jboss.weld.environment.se.Weld
import org.jboss.weld.environment.se.WeldContainer
import org.jboss.weld.resources.spi.ResourceLoader
import org.jglue.cdiunit.internal.Weld11TestUrlDeployment
import org.jglue.cdiunit.internal.WeldTestUrlDeployment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

import javax.naming.InitialContext

/**
 * Created by pbayer.*/
class CdiUnitListener extends AbstractRunListener {

	private static final Logger logger = LoggerFactory.getLogger(CdiUnitListener.class);

	private Weld weld
	WeldContainer container
	private Class classListeningTo
	private InitialContext initialContext

	CdiUnitListener(Class classListeningTo) {
		this.classListeningTo = classListeningTo
	}

	@Override
	void beforeSpec(SpecInfo spec) {
		logger.debug('BeforeSpec: entering')
		try {
			System.setProperty("java.naming.factory.initial", "org.jglue.cdiunit.internal.naming.CdiUnitContextFactory");
			weld = new Weld() {

				protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
					try {
						return new Weld11TestUrlDeployment(resourceLoader, bootstrap, classListeningTo);
					} catch (IOException ioExc) {
						throw new RuntimeException(ioExc);
					}
				}

				protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
					try {
						return new WeldTestUrlDeployment(resourceLoader, bootstrap, classListeningTo);
					} catch (IOException iOException) {
						throw new RuntimeException(iOException);
					}
				}
			};

		logger.debug('BeforeSpec: entering')
			logger.debug('BeforeSpec: Container started')
		} catch (Throwable var4) {
			throw new Exception("Unable to start weld", var4);
		}

	}

	@Override
	void beforeFeature(FeatureInfo feature) {
		this.container = this.weld.initialize();
		initialContext = new InitialContext()
		initialContext.bind("java:comp/BeanManager", container.getBeanManager());
		//		feature.spec.allFields.each {
		//			if (it.isAnnotationPresent(Inject) ) {
		//				println it
		//				it.writeValue(	feature.spec.	,container.instance().select(it.type).get())
		//			}
		//		}
	}

	@Override
	void afterFeature(FeatureInfo feature) {
		logger.debug('AfterFeature: entering')
		initialContext.close();
		weld.shutdown();
		logger.debug('AfterFeature: weld shut down')
	}
}
