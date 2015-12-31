/**
 *  Swann One Key Fob
 *
 *  v:1.00 - 12/31/2015 - Initial Configuraton
 *
 *  Copyright 2015 Rudimar Prunzel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Swann One Key Fob", namespace: "RudiP", author: "Rudimar Prunzel") {
		capability "Button"
		capability "Configuration"

        fingerprint profileId: "0104", deviceId: "0401", inClusters: "0000,0003,0500", outClusters: "0003,0501"
    }

    tiles {
	    standardTile("button", "device.button", width: 2, height: 2) {
		    state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
        }
    }
    main (["button"])
    details (["button"])
}

def parse(String description) {	       	            
    if (description?.startsWith('enroll request')) {        
        List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        def result = cmds?.collect { new physicalgraph.device.HubAction(it) }
        return result    
    } else if (description?.startsWith('catchall:')) {
        def msg = zigbee.parse(description)
        log.debug msg
        buttonPush(msg.data[0])
    } else {
        log.debug "parse description: $description"
    }    
}

def buttonPush(button){
    //Button Numbering vs positioning is slightly counterintuitive
    //Bottom Left Button (Unlock) = 0 and goes counterclockwise
    //Securifi Numbering - 0 = Unlock, 1 = * (only used to join), 2 = Home, 3 = Lock
    //For ST App Purposes 1=Lock, 2=Home, 3=Unlock , 4 = * (only used to join)
    def name = null
    if (button == 0) {
        //Home - ST Button 1
        name = "1"
        def currentST = device.currentState("button")?.value
        log.debug "Home button Pushed"           
    } else if (button == 3) {
    	//Away - ST Button 2
        name = "2"
        def currentST = device.currentState("button2")?.value
        log.debug "Away button pushed"        
    } else if (button == 2) {
        //Night - ST Button 3
        name = "3"
     	def currentST = device.currentState("button3")?.value
        log.debug "Night Button pushed"         
    } else {
        //Panic - ST Button 4
        name = "4"
     	def currentST = device.currentState("button4")?.value
        log.debug "Panic Button pushed"         
    
    }

    def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: name], descriptionText: "$device.displayName button $name was pushed", isStateChange: true)
    log.debug "Parse returned ${result?.descriptionText}"
    return result
}


def enrollResponse() {
    log.debug "Sending enroll response"
    [            
    "raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1"        
    ]
}


def configure(){
    log.debug "Config Called"
    def configCmds = [
    "zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
    "zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
    "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}"
    ]
    return configCmds
}
