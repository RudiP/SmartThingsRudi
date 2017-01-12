/**
*  WeMo AirPurifier (Device Handler)
*
*  v:1.0g - 01/11/2016 - Initial Version
*
*  Copyright 2017 Rudimar Prunzel
*  Based on the Holmes Humidifier code by Brian Keifer
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
    definition (name: "WeMo AirPurifier", namespace: "RudiP", author: "RudiP") {
        capability "Actuator"
        capability "Health Check"
        capability "Polling"
        capability "Refresh"
        capability "Switch"

        attribute "mode", "string"
        attribute "previousMode", "string"
        attribute "ionizer", "string"
        attribute "airQuality", "string"
        attribute "filterLife", "number"
        attribute "expiredFilterTime", "string"
        attribute "filterPresent", "string"

        command "fanOn"
        command "fanAuto"
        command "fanHigh"
        command "fanMed"
        command "fanLow"
        command "fanOff"
        command "fanNext"
        command "fanPrev"
        command "ionizerOn"
        command "ionizerOff"
        command "resetFilterLife"
    }

    preferences{
        section {
            input ("filterLifeDays", "number", title: "Expected Filter Life (in Days): ", defaultValue: 330, displayDuringSetup: false)
            input ("showLifeDays", "boolean", title: "Show Filter Life in Days?",  defaultValue:false, displayDuringSetup:false)
            input ("enableFilterReset", "boolean", title: "Enable Filter Reset?",  defaultValue:false, displayDuringSetup:false)
            input ("detailDebug", "boolean", title: "Enable Debug logging?",  defaultValue:false, displayDuringSetup:false)
        }
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles (scale: 2){
        multiAttributeTile(name: "status", type:"generic", width:6, height:4, canChangeIcon: true) {
            tileAttribute ("device.mode", key: "PRIMARY_CONTROL") {            		
                attributeState("Off", label:'Off', icon:"st.Appliances.appliances11", backgroundColor:"#bababa")
                attributeState("Low", label:'${currentValue}', icon:"st.Appliances.appliances11", backgroundColor:"#79b821")
                attributeState("Med", label:'Medium', icon:"st.Appliances.appliances11", backgroundColor:"#ffa81e")
                attributeState("High", label:'${currentValue}', icon:"st.Appliances.appliances11", backgroundColor:"#d04e00")
                attributeState("Auto", label:'${currentValue}', icon:"st.Appliances.appliances11", backgroundColor:"#1e9cbb")
            }
            tileAttribute("device.airQuality", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Air Quality: ${currentValue}')
            }
            tileAttribute("device.filterLife", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: "fanNext"
                attributeState "VALUE_DOWN", action: "fanPrev"
            }
        }

        standardTile("switch", "device.switch", decoration: "flat", canChangeBackground: true, width: 2, height: 2, canChangeIcon: true) {
            state "On", label:'ON', action:"fanOff", icon:"st.samsung.da.RC_ic_power", backgroundColor:"#79b821", nextState: "update"
            state "Off", label:'OFF', action:"fanOn", icon:"st.samsung.da.RC_ic_power", backgroundColor:"#bababa", nextState: "update"
            state "update", label:'Wait', icon:"st.samsung.da.RC_ic_power", backgroundColor:"#b59c60"
        }

        standardTile("ionizer", "device.ionizer", decoration: "flat", canChangeBackground: true, width: 2, height: 2, canChangeIcon: true) {
            state "On", label:'Ionizer: ON', action:"ionizerOff", icon:"st.Weather.weather7", backgroundColor:"#53a7c0", nextState: "update"
            state "Off", label:'Ionizer: OFF', action:"ionizerOn", icon:"st.Weather.weather7", backgroundColor:"#bababa", nextState: "update"
            state "update", label:'Wait', icon:"st.Weather.weather7", backgroundColor:"#b59c60"
        }

        standardTile("auto", "device.mode", decoration: "flat", canChangeBackground: true, width: 2, height: 2) {
            state "default", label: 'AUTO', action: "fanAuto", icon:"st.Appliances.appliances11",backgroundColor:"#ffffff", nextState: "update"
            state "Auto", label: 'AUTO', action: "fanOff", icon:"st.Appliances.appliances11",backgroundColor:"#1e9cbb", nextState: "update"
            state "update", label:'Wait', icon:"st.Appliances.appliances11", backgroundColor:"#b59c60"
        }

        standardTile("low", "device.mode", decoration: "flat", canChangeBackground: true, width: 2, height: 2) {
            state "default", label: 'LOW', action: "fanLow", icon:"st.Appliances.appliances11",backgroundColor:"#ffffff", nextState: "update"
            state "Low", label: 'LOW', action: "fanOff", icon:"st.Appliances.appliances11",backgroundColor:"#79b821", nextState: "update"
            state "update", label:'Wait', icon:"st.Appliances.appliances11", backgroundColor:"#b59c60"
        }

        standardTile("med", "device.mode", decoration: "flat", canChangeBackground: true, width: 2, height: 2) {
            state "default", label: 'MED', action: "fanMed", icon:"st.Appliances.appliances11",backgroundColor:"#ffffff", nextState: "update"
            state "Med", label: 'MED', action: "fanOff", icon:"st.Appliances.appliances11",backgroundColor:"#ffa81e", nextState: "update"
            state "update", label:'Wait', icon:"st.Appliances.appliances11", backgroundColor:"#b59c60"
        }

        standardTile("high", "device.mode", decoration: "flat", canChangeBackground: true, width: 2, height: 2) {
            state "default", label: 'HIGH', action: "fanHigh", icon:"st.Appliances.appliances11",backgroundColor:"#ffffff", nextState: "update"
            state "High", label: 'HIGH', action: "fanOff", icon:"st.Appliances.appliances11",backgroundColor:"#d04e00", nextState: "update"
            state "update", label:'Wait', icon:"st.Appliances.appliances11", backgroundColor:"#b59c60"
        }

        standardTile("filterPresent", "device.filterPresent", decoration: "flat", canChangeBackground: true, width: 2, height: 2, canChangeIcon: true) {
            state "yes", label:'Filter: OK', icon:"st.samsung.da.REF_3line_water_filter", backgroundColor:"#79b821"
            state "no", label:'Filter: N/A', icon:"st.samsung.da.REF_3line_water_filter", backgroundColor:"#d04e00"
        }

        standardTile("filterLife", "device.filterLife", width: 2, height: 2, decoration: "flat", canChangeBackground: true,  canChangeIcon: true) {
            state "filterLife", label:'Life: ${currentValue}', icon:"st.samsung.da.REF_3line_water_filter",
                  backgroundColors:[
                      [value: 10, color: "#bc2323"],
                      [value: 20, color: "#d04e00"],
                      [value: 30, color: "#90d2a7"],
                      [value: 50, color: "#79b821"]
                  ]

        }

        standardTile("resetFilterLife", "device.resetFilterLife", width: 2, height: 2, decoration: "flat") {
            state "defaut", label:'Reset Filter Life', action:"resetFilterLife", icon:"st.Health & Wellness.health7"
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main (["status"])
        details (["status","switch", "ionizer", "auto", "low", "med", "high", "filterPresent", "filterLife", "resetFilterLife", "refresh"])
    }
}

def installed() {
    log.info "--- Device Created"

}

def updated()
{
    log.info "--- Config Updated"
    state.debug = ("true" == detailDebug)
    state.lifeDays = ("true" == showLifeDays)

    refresh()
}

def poll() {
    refresh()
}

def refresh() {
    log.info("--- Refresh")
    subscribe()
    getAttributes()
}

def parse(String description) {

    def evtMessage = parseLanMessage(description)
    def evtHeader = evtMessage.header
    def evtBody = evtMessage.body

    if (evtBody) {
        evtBody = evtBody.replaceAll(~/&amp;/, "&")
        evtBody = evtBody.replaceAll(~/&lt;/, "<")
        evtBody = evtBody.replaceAll(~/&gt;/, ">")
    }

    // if (state.debug) log.debug("Header: ${evtHeader}")

    if (evtHeader?.contains("SID: uuid:")) {
        def sid = (evtHeader =~ /SID: uuid:.*/) ? ( evtHeader =~ /SID: uuid:.*/)[0] : "0"
        sid -= "SID: uuid:".trim()
        if (state.debug) log.debug "Subscription updated!  New SID: ${sid}"
        updateDataValue("subscriptionId", sid)
    }

    if (evtBody) {
        if (state.debug) log.debug("evtBody: ${evtBody}")
        def body = new XmlSlurper().parseText(evtBody)
        if (body == 0) {
            if (state.debug) log.debug ("Command succeeded!")
            return [getAttributes()]
        } else {
            log.info("Received: ${body.Body}")
            try {
                def matchResponse = body.Body =~ /Mode(\d)Ionizer(\d)AirQuality(\d)FilterLife(\d+)ExpiredFilterTime(\d)FilterPresent(\d)/
                if (state.debug) log.debug("Match Response: ${matchResponse[0]}")
                def result = []
                def mode
                def switchStatus = "On"
                def ionizer = "Off"
                def airQuality = "n/a"
                def filterLife = 0
                def filterLifeMins = (filterLifeDays ?: 330) * 24 * 60
                def filterUnit = "%"
                def expiredFilterTime = "No"
                def filterPresent = "No"

                switch(matchResponse[0][1].toInteger()) {
                    case 0:
                    mode = "Off"
                    switchStatus = mode
                    break
                    case 1:
                    mode = "Low"
                    break
                    case 2:
                    mode = "Med"
                    break
                    case 3:
                    mode = "High"
                    break
                    case 4:
                    mode = "Auto"
                    break
                }

                if (matchResponse[0][2].toInteger() == 1) {
                    ionizer = "On"
                }

                switch(matchResponse[0][3].toInteger()) {
                    case 0:
                    airQuality = "Poor"
                    break
                    case 1:
                    airQuality = "Moderate"
                    break
                    case 2:
                    airQuality = "Good"
                    break
                }

                if (matchResponse[0][5].toInteger() == 1) {
                    expiredFilterTime = "Yes"
                }

                if (matchResponse[0][6].toInteger() == 1) {
                    filterPresent = "Yes"
                }

                result += createEvent(name: "mode", value:mode)
                result += createEvent(name: "switch", value: switchStatus)
                result += createEvent(name: "ionizer", value:ionizer)
                result += createEvent(name: "airQuality", value:airQuality)

                if (state.lifeDays) {
                    filterLife = (matchResponse[0][4].toFloat() / 60 / 24).round(0).toInteger()
                    filterUnit = "d"
                } else {
                    filterLife = ((matchResponse[0][4].toFloat() / filterLifeMins) * 100).round(0).toInteger()
                    filterUnit = "%"
                }
                if (filterLife > 100) filterLife = 100;
                result += createEvent(name: "filterLife", value: filterLife, unit: filterUnit)

                result += createEvent(name: "expiredFilterTime", value: expiredFilterTime)
                result += createEvent(name: "filterPresent", value:filterPresent)

                return result
            } catch (e) {
                log.error("Exception: ${e}")
            }
        }
    }
}

//------ Device Functions -----------------

def fanOn() {
    setFan(device.latestState('previousMode').stringValue)
}

def fanAuto() { setFan("Auto")  }
def fanHigh() { setFan("High") }
def fanMed()  { setFan("Med")  }
def fanLow()  { setFan("Low")  }

def fanOff()  {
    def currentMode = device.latestState('mode').stringValue
    if (state.debug) log.debug("sending event: ${currentMode}")
    sendEvent(name: "previousMode", value: currentMode, displayed: false)
    setFan("Off")
}

def fanNext() { fanAuto() }
def fanPrev() { fanOff() }
def on()      { fanOn() }
def off()     { fanOff() }

def setFan(mode) {
    def newLevel = 0

    switch(mode) {
        case "Low":
          newLevel = 1
          break
        case "Med":
          newLevel = 2
          break
        case "High":
          newLevel = 3
          break
        case "Auto":
          newLevel = 4
          break
    }
    setAttribute("Mode", newLevel)
}

def ionizerOn() {
    setAttribute("Ionizer", 1)
}

def ionizerOff() {
    setAttribute("Ionizer", 0)
}

def resetFilterLife() {
    if (!("true" == enableFilterReset)) {
        log.info("--- Filter Reset NOT Enabled")
        return
    }
    def filterLifeMins = (filterLifeDays ?: 330) * 24 * 60
    setAttribute("FilterLife", filterLifeMins)
}

def setAttribute(name, value) {
    log.info("setAttribute($name, $value)")

    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:SetAttributes xmlns:u="urn:Belkin:service:deviceevent:1">
<attributeList>&lt;attribute&gt;&lt;name&gt;${name}&lt;/name&gt;&lt;value&gt;${value}&lt;/value&gt;&lt;/attribute&gt;</attributeList>
</u:SetAttributes>
</s:Body>
</s:Envelope>
"""
    postRequest('/upnp/control/deviceevent1', 'urn:Belkin:service:deviceevent:1#SetAttributes', body)
}


def getAttributes() {
    if (state.debug) log.debug("getAttributes()")
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetAttributes xmlns:u="urn:Belkin:service:deviceevent:1">
</u:GetAttributes>
</s:Body>
</s:Envelope>
"""
    postRequest('/upnp/control/deviceevent1', 'urn:Belkin:service:deviceevent:1#GetAttributes', body)
}

def getFilterUnit() {
    def filterUnit = "%"

    return filterUnit
}

//------ Network Functions -----------------

private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            if (state.debug) log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    return ip + ":" + port
}

private postRequest(path, SOAPaction, body) {
    // Send  a post request
    def result = new physicalgraph.device.HubAction([
        'method': 'POST',
        'path': path,
        'body': body,
        'headers': [
            'HOST': getHostAddress(),
            'Content-type': 'text/xml; charset=utf-8',
            'SOAPAction': "\"${SOAPaction}\""
        ]
    ], device.deviceNetworkId)
    return result
}

//------ Subscription Functions -----------------

def subscribe() {
    subscribe(getHostAddress())
}

def subscribe(hostAddress) {
    if (state.debug) log.debug "Subscribing to ${hostAddress}"
    subscribeAction("/upnp/event/basicevent1")
}

def subscribe(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        log.info "Updating ip from $existingIp to $ip"
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        log.info "Updating port from $existingPort to $port"
        updateDataValue("port", port)
    }

    subscribe("${ip}:${port}")
}

private subscribeAction(path, callbackPath="") {
    if (state.debug) log.debug "subscribe($path, $callbackPath)"
    def address = getCallBackAddress()
    if (state.debug) log.debug("address: ${address}")
    def ip = getHostAddress()

    def result = new physicalgraph.device.HubAction(
        method: "SUBSCRIBE",
        path: path,
        headers: [
            HOST: ip,
            CALLBACK: "<http://${address}/>",
            NT: "upnp:event",
            TIMEOUT: "Second-28800"
        ]
    )

    if (state.debug) log.trace "SUBSCRIBE $path"
    if (state.debug) log.trace "RESULT: ${result}"
    result
}

def resubscribe() {
    if (state.debug) log.debug "Executing resubscribe()'"
    def sid = getDeviceDataByName("subscriptionId")

    new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-5400


""", physicalgraph.device.Protocol.LAN)

}


def unsubscribe() {
    def sid = getDeviceDataByName("subscriptionId")
    new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}

//------ Generic Functions -----------------

private getCallBackAddress() {
    device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
