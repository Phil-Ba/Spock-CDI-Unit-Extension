package at.bayava.cdiunitextension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

import javax.enterprise.inject.Any
import javax.enterprise.inject.Produces
import javax.enterprise.inject.spi.Bean
import javax.enterprise.util.AnnotationLiteral
import javax.inject.Inject
/**
 * Created by pbayer.*/
class CdiUnitExtension extends AbstractAnnotationDrivenExtension<CdiUnit> {

	private static final Logger logger = LoggerFactory.getLogger(CdiUnitExtension.class);

	Map<String, Class> classes = [:]

	@Override
	void visitSpecAnnotation(CdiUnit annotation, SpecInfo spec) {
		//unfortunately I dont know any better way to determine the test class from here
		classes.(spec.filename) = this.class.classLoader.loadClass("$spec.package.$spec.filename" - '.groovy')
	}

	@Override
	void visitSpec(SpecInfo spec) {
		CdiUnitListener cdiUnitListener = new CdiUnitListener(classes.(spec.filename))
		spec.addListener(cdiUnitListener)
//		removeAllCdiFields(spec)
//		makeAllCdiFieldsShared(spec)
		spec.addInitializerInterceptor(new IMethodInterceptor() {

			@Override
			void intercept(IMethodInvocation iMethodInvocation) throws Throwable {
				println iMethodInvocation.method
				iMethodInvocation.proceed()
			}
		})
		spec.addSetupInterceptor(new IMethodInterceptor() {

			@Override
			void intercept(IMethodInvocation iMethodInvocation) throws Throwable {
				if (logger.traceEnabled) {
					Set<Bean<?>> beans = cdiUnitListener.container.beanManager.getBeans(Object.class, new AnnotationLiteral<Any>() {});
					for (Bean<?> bean : beans) {
						logger.debug('Bean in weld container: {}', bean.beanClass);
					}
				}
				final def instance = iMethodInvocation.instance
				iMethodInvocation.feature.parent.allFields.each {
					if (it.isAnnotationPresent(Inject)) {
						logger.info('Bean \'{}\' found for {}', cdiUnitListener.container.instance().select(it.type).get(), it.type)
						it.writeValue(instance, cdiUnitListener.container.instance().select(it.type).get())
					}
				}
				iMethodInvocation.proceed()
			}
		})
	}

	private boolean removeAllCdiFields(SpecInfo spec) {
		spec.allFields.removeAll {
			it.isAnnotationPresent(Inject) || it.isAnnotationPresent(Produces)
		}
	}

	private boolean makeAllCdiFieldsShared(SpecInfo spec) {
		spec.allFields.findAll  {
			it.isAnnotationPresent(Inject) || it.isAnnotationPresent(Produces)
		}.each {
			println it
		}
	}

}
