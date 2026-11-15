---
layout: event
title: "May meetup: faster JVM start-up techniques + Rust for Java developers"
slug: may-meetup-faster-jvm-start-up-techniques-rust-for-java-deve-44
dateDisplay: "May 30, 2022 at 18:00"
startDateTime: 2022-05-30T16:00Z
year: 2022
venueType: null
meetupUrl: https://www.meetup.com/java-vienna/events/285818563/
isUpcoming: false
hasDescription: true
talks:
- title: faster JVM start-up techniques
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Ionut Balosin
    slug: ionut-balosin-28
  resources:
  - type: SLIDES
    url: https://github.com/JavaVienna/2022-05-30_faster-jvm-start-up-techniques/blob/main/Techniques-for-a-faster-JVM-start-up.pdf
    title: Slides
  - type: CODE
    url: https://github.com/JavaVienna/2022-05-30_faster-jvm-start-up-techniques
    title: Source Code
- title: Rust for Java developers
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Stefan Baumgartner
    slug: stefan-baumgartner-55
  resources:
  - type: SLIDES
    url: https://github.com/JavaVienna/2022_05_30_rust_for_java_developers/blob/main/rust-for-java-developers_compressed.pdf
    title: Slides
sponsors:
- companyName: TU Wien
  logo: /img/f66a50d3-7849-47d9-97fd-0660bd61e5e0.jpg
  level: Room Sponsor
  websiteUrl: https://www.tuwien.ac.at
---

Agenda:

18:00 doors open
18:20 please grab drinks!
18:30 Faster JVM start-up techniques - Ionut Balosin
19:30 Break, Networking, Pizza
19:55 Raffle
20:00 Rust for Java developers - Stefan Baumgartner
21:10 Networking @ Pointers Sportsbar \(Resselgasse 5 \| A\-1040 Wien\)

#### **Techniques for a faster JVM start-up**

During this session, I will discuss a few techniques about how to improve the JVM start-up time for any application running on the JVM, independent of the programming language. These techniques could be applied in any project, in general without code changes. You can use the same techniques to speed up the JVM start-up time for your applications (both in development and production environments). Main topics:
\- when the JVM start\-up matters \(i\.e\. for what types of applications\)
\- Application/Dynamic class\-data sharing in HotSpot JVM
\- shared class cache in Eclipse OpenJ9
\- few handy JVM flags
\- hands\-on demonstration \(including benchmarks results\)
\- \(optional\) Ahead\-of\-time \(AOT\) compilation The JVMs included in this talk are OpenJDK HotSpot\, Eclipse OpenJ9 and Graal VM

**Rust for Java Developers**
Let's talk about Rust, the next programming language trying to do better than C++. Rust hit the top spot as Stack Overflow's most loved programming language for seven years in a row, and gets developers interested world-wide. But what's the deal with Rust? What are its use-cases? How does Rust keep its promises of zero-cost abstractions, memory safety without garbage collection, and fearless concurrency? In this session, we will go into the nitty-gritty details, compare Rust to what we know from other programming languages, and take a good look at its unique features.

Stefan Baumgartner is a software architect working at Dynatrace. He is the author of “TypeScript in 50 Lessons”, published by Smashing Magazine. He organizes ScriptConf, TSConf:EU, DevOne and Rust Linz, and co-hosts the German-language Working Draft podcast. Stefan enjoys Italian food, Belgian beer and British vinyl records.
