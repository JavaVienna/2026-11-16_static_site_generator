---
layout: event
title: "\"How to optimize JPA: The SQL Police\" + \"Using .js, .py+wasm in your java\
  \ apps\""
slug: how-to-optimize-jpa-the-sql-police-using-js-py-wasm-in-your-19
dateDisplay: "Jan 27, 2025 at 18:00"
startDateTime: 2025-01-27T17:00Z
year: 2025
venueType: null
meetupUrl: https://www.meetup.com/java-vienna/events/305515073/
isUpcoming: false
hasDescription: true
talks:
- title: "How to optimize JPA: The SQL Police"
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Philipp Seifert-Kehrer
    slug: philipp-seifert-kehrer-18
  resources:
  - type: SLIDES
    url: https://github.com/JavaVienna/2025-01-27_sql-police/blob/master/2025-01-27_Java-meetup-Philipp_sql-police.pdf
    title: Slides
  - type: CODE
    url: https://github.com/JavaVienna/2025-01-27_sql-police
    title: Source Code
- title: "The power of Graal: Using JavaScript, Python and WebAssembly in your Java\
    \ Apps"
  description: null
  durationMinutes: 45
  talkLanguage: en
  slidesLanguage: en
  speakers:
  - name: Dominik Dorn
    slug: dominik-dorn-4
  resources:
  - type: SLIDES
    url: https://github.com/JavaVienna/2025-01-27_graalvm_polyglot/blob/main/presentation/presentation.pdf
    title: Slides
  - type: CODE
    url: https://github.com/JavaVienna/2025-01-27_graalvm_polyglot
    title: Source Code
sponsors:
- companyName: dynatrace
  logo: https://secure.meetupstatic.com/photos/sponsor/a/5/0/6/iab120x90_2742246.jpeg
  level: Diamond
  websiteUrl: https://dynatrace.com
- companyName: Posedio GmbH
  logo: /img/1d5fcaba-89c1-4521-8986-871767af23c7.jpg
  level: Gold
  websiteUrl: https://posedio.com
- companyName: bitpanda
  logo: https://secure.meetupstatic.com/photos/sponsor/b/f/d/9/iab120x90_2749113.jpeg
  level: Gold
  websiteUrl: https://bitpanda.com
- companyName: TU Wien
  logo: /img/f66a50d3-7849-47d9-97fd-0660bd61e5e0.jpg
  level: Room Sponsor
  websiteUrl: https://www.tuwien.ac.at
---

save the date

Agenda:
18:00 doors open
18:25 How to optimize JPA: The SQL Police - Philipp Seifert-Kehrer
19:30 break
19:55 raffle
20:00 The power of Graal: Using JavaScript, Python and WebAssembly in your Java Apps - Dominik Dorn
21:10 networking

**Going KonMari on your SQL Queries with the SQL-Police**
Let’s dive into the mess that we get ourselves into when naively relying on JPA and its implementations and see how to generate nothing else but SQL statements that spark joy!
Spring Data, JPA, Hibernate got our backs though to avoid messy database access, right? HELL NO! All they do is teach you a lesson to not rely on other systems without verifying the fundamentals functioning as expected yourself! There is tradeoffs with every framework and you do not want to iron out those kinks in production.
We will look at common anti-patterns that we tend to apply when relying on the JPA-ecosystem and how to regain control over the database layer of your applications again.

**The power of Graal: Using JavaScript, Python and WebAssembly in your Java Apps**
"One VM to rule them all" was a talk given a few years ago at the Java VIenna Meetup. Often we hear about Graal allowing other languages
to run on the JVM, but have you ever seen it?
In this talk we'll take a look on how you can integrate libraries from
the JavaScript and Python universum, how you can allow scripting in your apps and how you can use WebAssembly (and by that, every language that can be compiled to it) from your Java application.
