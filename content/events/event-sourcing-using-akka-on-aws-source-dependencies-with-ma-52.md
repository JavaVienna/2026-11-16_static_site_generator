---
layout: event
title: Event Sourcing using Akka on AWS + source dependencies with Maven and
slug: event-sourcing-using-akka-on-aws-source-dependencies-with-ma-52
dateDisplay: "May 8, 2017 at 18:15"
startDateTime: 2017-05-08T16:15Z
year: 2017
venueType: null
meetupUrl: https://www.meetup.com/java-vienna/events/238559105/
isUpcoming: false
hasDescription: true
talks:
- title: Event Sourcing using Akka on AWS
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Daniel Pfeiffer
    slug: daniel-pfeiffer-79
  resources:
  - type: CODE
    url: https://github.com/JavaVienna/2017-05-08_event-sourcing-aws-akka-showcase-java
    title: Source Code
- title: Source Dependencies with Maven and Gradle
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Peter Palaga
    slug: peter-palaga-84
  resources: []
sponsors:
- companyName: TU Wien
  logo: /img/f66a50d3-7849-47d9-97fd-0660bd61e5e0.jpg
  level: Room Sponsor
  websiteUrl: https://www.tuwien.ac.at
---

Agenda:

• 18:00 doors open

• 18:30 - 19:00 Mini-Talk: Srcdeps - source dependencies with Maven and Gradle

• 19:00 - 19:30 food, drinks, networking

• 19:30 - 20:30 Event Sourcing using Akka on AWS

The talks are:

Talk: Event Sourcing using Akka on AWS (60')

"'Capture all changes to an application state as a sequence of events' is what Martin Fowler said about Event Sourcing in 2005 and what is the starting point into that topic for this talk.

I will demonstrate how you can store events using Akka Persistence and then distribute them via AWS to be consumed by your other services.

An event based architecture has lots of technical and organisational benefits for your development team. It can be a huge gain for your development process, but can also be difficult to implement as there are lots of challenges.

I will discuss the good as well as the bad things and provide solutions to overcome common pitfalls and aforementioned challenges.

About the Speaker:

Daniel Pfeiffer

Mini-Talk: Srcdeps - source dependencies with Maven and Gradle (30')

While the idea of source dependencies is not new -- some languages (Ruby, ...) have it for long -- it is new in the field of Java and its build tools.

With srcdeps, a Maven or Gradle project can define its dependencies in terms of specific source commits that are built by srcdeps "on the fly" when they are requested.

Clearly, such source dependencies do not need to be present in any remote Maven repository, read: they do not need to be released.

We will show how this can be useful in testing and integration or for fixing third-party code. We will not forget to compare srcdeps with Maven SNAPSHOTS.

While the implementation of Srcdeps for Maven exists since 2015, the Gradle support is brand new and will be presented for the first time publicly at this Java-Vienna Meetup.

About the Speaker:

Peter Palaga is a Senior Sustaining Engineer for JBoss EAP at Red Hat and the main author of srcdeps. Maven builds and maintenance of complex dependency hierarchies belong to his daily duties.
