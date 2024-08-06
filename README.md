# IPA Keyboard Router Server
This Server, written in Java, handles actually sending data and confirmations to and from the [Client](https://github.com/codeBodger/IPA_Keyboard_Client_with_Robot?tab=readme-ov-file#readme).  When it is sent data from the [Website Backend](https://github.com/codeBodger/IPA_Keyboard_Website_Backend?tab=readme-ov-file#readme) (which got the data from the [Website](https://github.com/codeBodger/IPA_Keyboard_Website?tab=readme-ov-file#readme)), and splits it into the key-byte and the long-code corresponding to the specific [Client](https://github.com/codeBodger/IPA_Keyboard_Client_with_Robot?tab=readme-ov-file#readme).  The long-code is looked up in a dictionary of clients, and the proper client is sent the byte.  If such a request is not sent for an hour, the client is removed from the dictionary, to ensure that there is not a memory leak. 

Depending on what this Router Server is sent and from where it will do different things.  It will give the [Backend](https://github.com/codeBodger/IPA_Keyboard_Website_Backend?tab=readme-ov-file#readme) the long-code for a given linking-code when requested, or it will pass on the key-byte.  Either way, it sends a response (and sometimes additional information) back to the [Client](https://github.com/codeBodger/IPA_Keyboard_Client_with_Robot?tab=readme-ov-file#readme) (and [Backend](https://github.com/codeBodger/IPA_Keyboard_Website_Backend?tab=readme-ov-file#readme) when that was the origin of the request) indicating the degree of success.  

## Definitions
|               |                                                                                        |
| ------------- | -------------------------------------------------------------------------------------- |
| Key‑code:     | the three digit<sub>10</sub> (000-164) code representing a single IPA character        |
| Key‑byte:     | the key-code as a single byte (typically implicitly casted to `int`)                   |
| Long‑code:    | the 18 digit<sub>64</sub> code to indicate which client a key-byte needs to be sent to |
| Linking‑code: | the six digit<sub>10</sub> code to get the long-code from Router Server to Website     |
