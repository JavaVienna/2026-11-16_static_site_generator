---
layout: event
title: "Webswing & webforJ in der Praxis & Here we go again: Java26"
slug: webswing-webforj-in-der-praxis-here-we-go-again-java26-88
dateDisplay: "Apr 7, 2026 at 18:00"
startDateTime: 2026-04-07T16:00Z
year: 2026
venueType: null
meetupUrl: https://www.meetup.com/java-vienna/events/313196243
isUpcoming: false
hasDescription: true
talks: []
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
18:00 Doors Open
18:20 Intro/Sponsor Welcome
18:30 Doors Closed! (the reception on the ground floor will not be served anymore.. come early!)
18:30 **Here we go again: Java26 / Dominik Dorn**
19:30 Break, Food, Drinks
19:55 Raffle
20:00 **Java-UIs modernisieren ohne JavaScript — Webswing & webforJ in der Praxis / Stephan Wald**
21:00 Networking

Talk 1: **Here we go again: Java 26**
We'll explore everything new in Java 26!

**Java-UIs modernisieren ohne JavaScript — Webswing & webforJ in der Praxis / Stephan Wald**

Ihr habt eine Swing-Anwendung. Oder Eclipse RCP. Oder Oracle Forms. Das Management will sie „im Browser". Der Reflex: React-Frontend, REST-APIs, JavaScript-Entwickler einstellen, zwei Stacks pflegen. Aber muss das sein?
Es gibt inzwischen mehrere Java-native Ansätze für Web-UIs — ihr kennt vermutlich einige davon aus früheren Meetups. Die meisten setzen voraus, dass ihr eure UI von Grund auf neu schreibt und bestehenden Code hinter REST-APIs kapselt. Unser Ansatz ist ein anderer: Wir zeigen einen **zweistufigen Migrationspfad**, der mit der unveränderten Bestandsanwendung im Browser startet und die Modernisierung dann Bildschirm für Bildschirm vorantreibt — ohne den Betrieb zu unterbrechen und ohne eine zweite Technologie einzuführen.
Im ersten Schritt kommt **Webswing** zum Einsatz — eine Laufzeitumgebung, die Java-Desktop-Anwendungen (Swing, JavaFX, SWT, Eclipse RCP) unverändert im Browser bereitstellt, indem sie auf Toolkit-Ebene in die AWT-Pipeline eingreift und das Rendering über HTML5 Canvas und WebSocket an den Client streamt. Im zweiten Schritt zeigen wir **webforJ** — ein Open-Source-Framework (Apache 2.0), mit dem moderne Web-UIs komponentenbasiert in purem Java entstehen: Routing, Data Binding, responsive Layouts, Spring-Boot-Integration — ohne eine Zeile JavaScript.
Der technische Kern des Abends:

* **Webswing unter der Haube:** Wie die Custom-`Toolkit`-Implementierung funktioniert, was auf Server- vs. Client-Seite passiert, und wo die Latenz-Grenzen liegen
* **webforJ-Architektur:** Server-seitiges Component Model auf Basis von DWC (Dynamic Web Client) Web Components, Event-Handling im Vergleich zum Swing-Listener-Modell, Layout-System (`FlexLayout` vs. `BorderLayout`)
* **`WebswingConnector`:** Die Integrationsbrücke — wie eine webforJ-Anwendung eine Webswing-Session einbettet und bidirektionale Kommunikation zwischen Legacy-Swing-Code und modernem UI ermöglicht
* **Live-Code:** Vom minimalen webforJ-Hello-World bis zur Hybrid-App mit eingebettetem Swing-Panel — wir bauen es vor euren Augen
* **Ehrliche Trade-offs:** Wann dieser Ansatz passt, wann nicht — und was er im Vergleich zu einem reinen UI-Framework-Wechsel bringt und kostet

Wir bringen einen Praxisbericht mit: Ein niederländischer ERP-Anbieter hat mit einem kleinen Team in vier Monaten die ersten Web-Module produktiv geschaltet — ohne JavaScript-Expertise im Haus.

**About the Speaker**
Stephan Wald entwickelt seit über 35 Jahren Enterprise-Software, seit über 20 Jahren in Java. Seine Erfahrung umfasst C++, C#, mehrere Business-Basic-Dialekte sowie heute primär Java. Als Head of Development und Mitglied der Geschäftsleitung bei BASIS International Ltd. (Albuquerque, USA) verantwortet er die Entwicklung des webforJ-Frameworks, eines Single-Stack-UI-Frameworks für geschäftskritische Java- und Kotlin-Anwendungen in mittelständischen und großen Unternehmen weltweit. Parallel baut er mit [SkillsPilot.de](http://skillspilot.de/) eine KI-gestützte SaaS-Plattform im HR-Consulting, die mit namhaften deutschen Unternehmen mit bis zu 20.000 Mitarbeitenden wächst. Speaker auf der JavaOne 2026.
