/*
 * Copyright (c) 2019-2022 Tomasz Babiuk
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.automateeverything.onoffplugin

import eu.automateeverything.data.automation.State
import eu.automateeverything.data.instances.InstanceDto
import eu.automateeverything.data.localization.Resource
import eu.automateeverything.domain.automation.AutomationUnit
import eu.automateeverything.domain.configurable.*
import eu.automateeverything.domain.events.EventBus
import eu.automateeverything.domain.hardware.PortFinder
import eu.automateeverything.domain.hardware.Relay
import java.util.*
import org.pf4j.Extension

@Extension
class OnOffDeviceConfigurable(private val portFinder: PortFinder, private val eventBus: EventBus) :
    StateDeviceConfigurable() {

    override val fieldDefinitions: Map<String, FieldDefinition<*>>
        get() {
            val result: LinkedHashMap<String, FieldDefinition<*>> =
                LinkedHashMap(super.fieldDefinitions)
            result[FIELD_PORT] = portField
            result[FIELD_AUTOMATION_ONLY] = automationOnlyField
            return result
        }

    override val parent: Class<out Configurable>
        get() = OnOffDevicesConfigurable::class.java

    override val addNewRes: Resource
        get() = R.configurable_onoffdevice_add

    override val editRes: Resource
        get() = R.configurable_onoffdevice_edit

    override val titleRes: Resource
        get() = R.configurable_onoffdevice_title

    override val descriptionRes: Resource
        get() = R.configurable_onoffdevices_description

    override val iconRaw: String
        get() =
            """<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
                 <g>
                  <title>Layer 1</title>
                  <path fill="black" d="m4,53.572c-0.016,-12.872 5.321,-22.61 10.48,-28.792l0,0c5.187,-6.233 10.224,-9.236 10.676,-9.516l0,0c3.368,-1.993 7.697,-0.849 9.667,2.555l0,0c1.966,3.392 0.851,7.747 -2.489,9.75l0,0l0.001,0.001c0,0 -0.004,0.001 -0.015,0.008l0,0c-0.009,0.004 -0.023,0.014 -0.042,0.026l0,0c-0.068,0.042 -0.194,0.125 -0.367,0.244l0,0c-0.351,0.234 -0.897,0.625 -1.572,1.162l0,0c-1.349,1.073 -3.203,2.74 -5.029,4.945l0,0c-3.669,4.467 -7.166,10.798 -7.181,19.616l0,0c0.003,8.889 3.547,16.882 9.314,22.722l0,0c5.78,5.83 13.687,9.416 22.48,9.418l0,0c8.793,-0.002 16.699,-3.588 22.478,-9.418l0,0c5.769,-5.84 9.313,-13.833 9.316,-22.722l0,0c-0.01601,-8.548 -3.29401,-14.743 -6.83701,-19.195l0,0c-3.37299,-4.221 -6.98199,-6.572 -7.341,-6.791l0,0c-0.004,-0.003 -0.009,-0.004 -0.012,-0.006l0,0c-0.005,-0.003 -0.008,-0.005 -0.01099,-0.006l0,0c-0.00301,-0.003 -0.007,-0.003 -0.007,-0.003l0,0l0,0c-3.336,-2.005 -4.452,-6.36 -2.488,-9.752l0,0c1.972,-3.404 6.3,-4.548 9.667,-2.555l0,0c0.45399,0.279 5.491,3.282 10.67799,9.516l0,0c5.16199,6.183 10.49599,15.92 10.481,28.792l0,0c-0.004,25.637 -20.564,46.42 -45.923,46.429l0,0c-25.359,-0.009 -45.918,-20.792 -45.924,-46.428l0,0l0.00002,0z" id="svg_1"/>
                  <path d="m42.859,50.001l0,-21.43l0,-21.428c0,-3.944 3.163,-7.143 7.066,-7.143l0,0c3.899,0 7.062,3.199 7.062,7.143l0,0l0,21.428l0,21.43l0.002,0c0,3.941 -3.165,7.141 -7.064,7.141l0,0c-3.903,0 -7.066,-3.2 -7.066,-7.141l0,0z" id="svg_2"/>
                 </g>
                </svg>"""

    private val portField =
        RelayOutputPortField(FIELD_PORT, R.field_port_hint, RequiredStringValidator())
    private val automationOnlyField =
        BooleanField(FIELD_AUTOMATION_ONLY, R.field_automation_only_hint, false)

    override fun buildAutomationUnit(instance: InstanceDto): AutomationUnit<State> {
        val portId = extractFieldValue(instance, portField)
        val port = portFinder.searchForOutputPort(Relay::class.java, portId)
        val name = instance.fields[FIELD_NAME]!!
        val automationOnly = extractFieldValue(instance, automationOnlyField)
        return OnOffDeviceAutomationUnit(eventBus, instance, name, states, port, automationOnly)
    }

    override val states: Map<String, State>
        get() {
            val states: MutableMap<String, State> = HashMap()
            states[STATE_INIT] =
                State.buildReadOnlyState(
                    STATE_INIT,
                    R.state_unknown,
                )
            states[STATE_ON] =
                State.buildControlState(STATE_ON, R.state_on, R.action_on, isSignaled = true)
            states[STATE_OFF] =
                State.buildControlState(
                    STATE_OFF,
                    R.state_off,
                    R.action_off,
                )
            return states
        }

    companion object {
        const val FIELD_PORT = "portId"
        const val FIELD_AUTOMATION_ONLY = "automationOnly"
        const val STATE_ON = "on"
        const val STATE_OFF = "off"
    }
}
