---
layout: event
title: "\"living immutable: painless Java Records\" + \"Zero-Deps Java AI Business\
  \ Agents\""
slug: living-immutable-painless-java-records-zero-deps-java-ai-bus-94
dateDisplay: "Jun 29, 2026 at 18:00"
startDateTime: 2026-06-29T16:00Z
year: 2026
venueType: null
meetupUrl: https://www.meetup.com/java-vienna/events/314639734
isUpcoming: false
hasDescription: true
talks:
- title: "living immutable: working with Java Records the painless way"
  description: |-
    Java Records are great for immutability, but updating deeply nested structures usually means dealing with a headache of nested constructors and endless *.withX()* chains.

    This talk covers how to skip the boilerplate using Telescope, a lightweight Java library designed for painless deep updates. We will dive into:

    * The ergonomic pain points of nested Java Records.
    * Using Telescope's fluent DSL for surgical, zero-boilerplate updates
    * Applying these same clean immutability patterns to legacy POJOs.
  durationMinutes: 60
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Georgios Angelopoulos
    slug: georgios-angelopoulos-103
  resources: []
- title: "Zero-Dependency Java 25+ Business Agents: Running locally with zsmith and\
    \ LightMetal"
  description: |-
    Modern Java provides all the functionality required for local LLM inference and agent orchestration, eliminating the need for external dependencies.

    LightMetal directly binds Llama.cpp from Java using the **Foreign Function and Memory API (FFM)**, eliminating the need for JNI or native wrappers. It runs open models such as **Mistral, Gemma, and Llama** on your machine.
    Zsmith is built on LightMetal and uses the Boundary-Control-Entity (BCE) model to structure agents. It loads tools and skills as ServiceLoader plugins and runs tool calls and streaming on Virtual Threads. Local inference and remote models work with the same agent code over the JDK's HttpClient.

    Topics:
    \- One\-line installation\, no build tools\, no package managers
    \- FFM in practice: calling native libraries without JNI
    \- Running Mistral\, Gemma and Llama locally in pure Java
    \- ServiceLoader as a plugin mechanism for tools and skills
    \- BCE applied to AI agents
    \- Virtual Threads for streaming\, tool calls and parallel inference
    \- Switching between local and remote models without changing the agent
    \- Plain JDK: HttpClient\, records\, sealed types and pattern matching
    \- Packaging agents as single\-file Java applications

    Everything is demonstrated live with code. Your questions are highly welcome — the more, the better!
  durationMinutes: 60
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Adam Bien
    slug: adam-bien-19
  resources: []
sponsors:
- companyName: dynatrace
  logo: https://secure.meetupstatic.com/photos/sponsor/a/5/0/6/iab120x90_2742246.jpeg
  level: Diamond
  websiteUrl: https://dynatrace.com
- companyName: Posedio GmbH
  logo: /img/1d5fcaba-89c1-4521-8986-871767af23c7.jpg
  level: Gold
  websiteUrl: https://posedio.com
---

Agenda:
18:00 Doors open
18:20 welcome - doors closing
18:30 "living immutable: working with Java Records the painless way" - Georgios Angelopoulos
\~ 19:00/19:15 break/networking
\~ 19:30/19:45 Zero-Dependency **Java 25+ AI Business Agents**: Running locally with zsmith and LightMetal - Adam Bien

**living immutable: working with Java Records the painless way**
Java Records are great for immutability, but updating deeply nested structures usually means dealing with a headache of nested constructors and endless *.withX()* chains.

This talk covers how to skip the boilerplate using Telescope, a lightweight Java library designed for painless deep updates. We will dive into:

* The ergonomic pain points of nested Java Records.
* Using Telescope's fluent DSL for surgical, zero-boilerplate updates
* Applying these same clean immutability patterns to legacy POJOs.

**About Georgios Angelopoulos**
I am a Lead Developer at Prewave with a background in Electrical and Computer Engineering. My experience and interests cover the entire computing spectrum, solving problems at every layer of the stack.

**Zero-Dependency Java 25+ Business Agents: Running locally with zsmith and LightMetal**
Modern Java provides all the functionality required for local LLM inference and agent orchestration, eliminating the need for external dependencies.

LightMetal directly binds Llama.cpp from Java using the **Foreign Function and Memory API (FFM)**, eliminating the need for JNI or native wrappers. It runs open models such as **Mistral, Gemma, and Llama** on your machine.
Zsmith is built on LightMetal and uses the Boundary-Control-Entity (BCE) model to structure agents. It loads tools and skills as ServiceLoader plugins and runs tool calls and streaming on Virtual Threads. Local inference and remote models work with the same agent code over the JDK's HttpClient.

Topics:
\- One\-line installation\, no build tools\, no package managers
\- FFM in practice: calling native libraries without JNI
\- Running Mistral\, Gemma and Llama locally in pure Java
\- ServiceLoader as a plugin mechanism for tools and skills
\- BCE applied to AI agents
\- Virtual Threads for streaming\, tool calls and parallel inference
\- Switching between local and remote models without changing the agent
\- Plain JDK: HttpClient\, records\, sealed types and pattern matching
\- Packaging agents as single\-file Java applications

Everything is demonstrated live with code. Your questions are highly welcome — the more, the better!

**About Adam Bien**
Entwickler, Consultant, manchmal Konferenzsprecher, Autor und Java / Jakarta (SE/EE) / MicroProfile Enthusiast.
Adam Bien ([adambien.blog](http://adambien.blog)) arbeitet mit Java seit JDK 1.0, JavaScript seit LiveScript und hat immer noch sehr viel Spass am Programmieren. Adam veranstaltet regelmässig Java EE / WebStandards / JavaScript Workshops am Flughafen München ([http://airhacks.com](http://airhacks.com)).
