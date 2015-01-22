Hand-e-Mapper
=============
Project detects a users hand gestures of moving, grabbing, dragging, and dropping using a basic web camera. This implements the Java version of OpenCV and once these gestures can be recognized reliably, they will be mapped to the mouse input device.

This GitHub Repo
----------------
This repository contains the Apache Maven parent project which includes; commons, demonstration GUI, OpenCV dynamic library loader, and the recognition sub-projects. It is recommended to implement a separate GUI and exclude the GUI project dependency and simply use the interfaces provided in the commons sub-project.

Project Background and Overview
-------------------------------
This project involves a joint collaboration between the Computer Science Department at California Polytechnic State University in San Luis Obispo and the Intel Corporation. Use of this software is detailed under the BSD 2-Clause License (http://opensource.org/licenses/BSD-2-Clause). Our contact from Intel provides the primary focus and objectives for this project which involves the use of a basic web camera to detect the gesture of "throwing" virtual objects on a computer and/or mobile device. This focus requires the use of computer vision software to attempt to detect the generalized gestures of throwing.
