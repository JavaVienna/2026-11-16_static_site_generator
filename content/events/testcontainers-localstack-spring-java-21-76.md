---
layout: event
title: '"Testcontainers + LocalStack + Spring" & "Java 21"'
slug: testcontainers-localstack-spring-java-21-76
dateDisplay: "Jan 8, 2024 at 18:00"
startDateTime: 2024-01-08T17:00Z
year: 2024
venueType: null
meetupUrl: https://www.meetup.com/java-vienna/events/296033487/
isUpcoming: false
hasDescription: true
talks:
- title: "TestContainers, Localstack + Spring"
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Anca Ghenade
    slug: anca-ghenade-32
  resources:
  - type: SLIDES
    url: https://github.com/JavaVienna/2024-01-08_Slides/blob/main/Taming%20and%20Testing%20the%20Cloud%20Environment%20for%20your%20Java%20apps.pdf
    title: Slides
  - type: CODE
    url: https://github.com/JavaVienna/2024-01-08_testcontainers-shipment-list-cloud-pods
    title: Cloud Pods Code
  - type: CODE
    url: https://github.com/JavaVienna/2024-01-08_testcontainers-shipment-list-demo
    title: Demo Code
- title: Java 21
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Dominik Dorn
    slug: dominik-dorn-4
  resources:
  - type: SLIDES
    url: https://github.com/JavaVienna/2024-01-08_Slides/blob/main/whatsnewinjava-17to21.pdf
    title: Slides
  - type: CODE
    url: https://github.com/JavaVienna/2024-01-08_WhatsNewInJava21
    title: Source Code
sponsors:
- companyName: dynatrace
  logo: https://secure.meetupstatic.com/photos/sponsor/a/5/0/6/iab120x90_2742246.jpeg
  level: Diamond
  websiteUrl: https://dynatrace.com
- companyName: OpenForce GmbH
  logo: https://openforce.com/assets/Uploads/openFORCE-v2.svg
  level: Gold
  websiteUrl: https://openforce.com/
- companyName: Posedio GmbH
  logo: /img/1d5fcaba-89c1-4521-8986-871767af23c7.jpg
  level: Gold
  websiteUrl: https://posedio.com
- companyName: TU Wien
  logo: /img/f66a50d3-7849-47d9-97fd-0660bd61e5e0.jpg
  level: Room Sponsor
  websiteUrl: https://www.tuwien.ac.at
---

save the date

Agenda:
18:00 Doors open
18:20 Partner introduction, organization stuff
18:30 Taming and Testing the Cloud Environment for your Java apps with LocalStack and Testcontainers - Anca Ghenade
19:30 Networking break
20:00 Java 21 - Dominik Dorn
21:05 Networking @ Pointers

**Taming and Testing the Cloud Environment for your Java apps with LocalStack and Testcontainers**
So, you use AWS in production, but how do you develop and test your AWS-powered Java applications? Navigating the development and testing landscape of cloud-based applications can be a complex task. In this presentation, we delve into the transformative approach of using LocalStack as a drop-in replacement for AWS services in the development phase of Spring Boot applications. We see how LocalStack, a fully functional local AWS cloud stack, seamlessly integrates into your existing setup, offering a robust and cost-effective environment for development. This method not only enhances developer productivity but also ensures consistency with AWS cloud environments, with minimal changes. As the application development progresses and gains complexity, the necessity for rigorous testing emerges. Here, we transition to exploring the integration of Testcontainers with LocalStack, as an official module. We'll have a look at how you can quickly set up your Spring Boot application and AWS stack for integration testing using Testcontainers, and additionally, we will discuss some best practices for writing effective integration tests for your AWS-powered application, emphasizing the importance of maintaining a balance between test coverage and test maintainability.

**About Anca**
Anca has been immersing herself in the realm of Java since her university days, focusing primarily on developing enterprise applications over the past seven years. Recently, she has embraced the exciting and challenging field of cloud development, captivated by its immense potential. Now she enjoys sharing her learning experiences through engaging demos that bring tangible value to others. She also appreciates the meaningful conversations and feedback related to user experience and use cases, as they enable her to contribute towards the product development efforts of her team at LocalStack.

**Java 21**
We will take a look at what has changed/is new from JDK17 to 21.
