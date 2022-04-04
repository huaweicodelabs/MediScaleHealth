# MediScale Health

# Huawei Mobile Services
Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.

## Table of Contents
* [Introduction](#introduction)
* [What you will Create](#what-you-will-create)
* [What You Will Learn](#what-you-will-learn)
* [Hardware Requirements](#hardware-requirements)
* [Software Requirements](#software-requirements)
* [License](#license)

## Introduction
    
Mediscale tracks user's movement to provide important stats and triggers an alarm to inform all the saved contacts during a fall injury. The program primarily focuses 
on tracking users movement, but also showcases the following HMS kits capibilities.

## What You Will Create

In this code lab, you will create a MediScale Health project and use the APIs of Location, Map, Site and ML kits, you can explore the following processes in the MediScale Health project.

*	Detect if user has a fall injury
*	Obtain weather and AQI based on location
*	Help users analyze various health parameters like BMI and users activity such as walking and running
*	Allow users to activate SOS trigger function to alert the saved contacts

## What You Will Learn

In this code lab, you will learn how to:
*	Map Kit
*	Location Kit
*	Site Kit
*	ML Kit

## What You Will Need

### Hardware Requirements

*	A computer (desktop or laptop) that runs the Windows 10 operating system
*	Huawei phone with HMS Core (APK) 5.0.0.300 or later installed
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*	[Android Studio 3.X](https://developer.android.com/studio)
*	JDK 1.8 and later 
*	SDK Platform 23 and later
*	Gradle 4.6 and later


## Prepare Initial configuration

Use the below link to do initial configuration for the application development
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3

## Enable HUAWEI Service(s) in AGC console

Enable the API permission for below kits from Project Settings, Manage APIs and enable the API permission.
*	Map Kit
*	Location Kit
*	Site Kit
*	ML Kit

```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website.

*  [Map Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-introduction-0000001061991291)

*  [Location Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050706106)

*  [Site Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-about-the-service-0000001076188604)

*  [ML Kit](https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/service-introduction-0000001050040017)


## License
HMS Guide sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).