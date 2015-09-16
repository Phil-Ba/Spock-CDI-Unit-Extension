package at.bayava.cdiunitextension
import org.jglue.cdiunit.ProducesAlternative
import spock.lang.Specification

import javax.enterprise.inject.Produces
import javax.inject.Inject
/**
 * Created by pbayer.*/

@CdiUnit
class CdiUnitExtensionTest extends Specification {

	@Produces
	@ProducesAlternative
	Person test = new Person(name:'fooBar')

//	@Produces
//	Person test3 = new Person(name:'Hans')

	@Inject
	Person test2

	def "CdiUnitListener is added to spec annotated with @CdiUnit"() {
		expect:
		this.specificationContext.currentSpec.listeners.size() == 1
		this.specificationContext.currentSpec.listeners[0] instanceof CdiUnitListener
	}

	def "String is injected"() {
		expect:
		test2
		test2.name == 'fooBar'
	}

}