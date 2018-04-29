
# Transport
A simple approach and lightweight implementation for handling UDP/TCP in Java/Android application.

This project also added simple Servers implemented in Java to interacting with client. You can find the servers int corresponding package of UDP/TCP. Each UDP/TCP package is devided into two sub-packaces client and server. For masharling/demarshaling data you can simply replace your implenentation. Currently, this project has demo 2 kind of serialize/deserialize using raw string and protobuf to demonstrate transferring data beetween client and server.

The UDP client was written in 2 versions. The normal version is simple concept as UDP does, it is connectionless. The v2 version is connection concept, the core concept of it is simply treat UDP as a virtual connection and we can observe the connection as well. This concept was inspired by [Apache Mina Project](https://mina.apache.org/).

## License

    Copyright (C) 2017 thuannv

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.