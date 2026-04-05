# p100fx / p101fx

## 🔷 Overview

p100fx is an experimental UI engine built with JavaFX.

It explores a different approach to building user interfaces:

> UI is not defined in code — it is defined as data.

---

## 🔷 Core Idea

Traditional UI:

```
UI → Code → Compile → Run
```

p100fx:

```
UI → Data → Load → Render
```

The interface is stored externally, loaded at runtime, and interpreted by the system.

---

## 🔷 Design Philosophy

This project does not aim to define a fixed implementation.

Instead, it presents a way of thinking:

* UI is not a collection of components
* UI is a system defined by data
* Interaction is not tied to objects, but to context

The exact implementation may vary.

Each developer is free to interpret and extend these ideas in their own way.

---

## 🔷 Key Principles

* **Data over Code**
  UI behavior is driven by data structures

* **Context over Components**
  Meaning comes from how a region is interpreted

* **Behavior over Structure**
  Actions emerge from interaction, not predefined classes

---

## 🔷 Interaction Concept

The system does not rely on traditional UI components such as buttons or text fields.

Instead, the interface is composed of **regions**.

A region may represent:

* navigation
* an action
* an input area

Behavior is not hardcoded.

> Behavior emerges from data, not from predefined components.

---

## 🔷 Runtime Behavior

At runtime:

1. Data is loaded
2. The interface is rendered
3. User interaction is interpreted
4. The system responds based on context

There is no fixed mapping like:

```
Button → onClick
```

Instead:

```
Region → Context → Behavior
```

---

## 🔷 Architecture Direction

The system moves logic away from code and into data.

This allows:

* dynamic UI updates
* flexible interaction models
* reduced dependency on frameworks

---

## 🔷 Origin

This project is inspired by an earlier system developed in 1996–1997.

That system explored similar ideas in a DOS environment.

👉 See LPSTART repository for the original concept.

---

## 🔷 What This Project Is

* a UI engine experiment
* a design exploration
* a foundation for alternative UI systems

---

## 🔷 What This Project Is NOT

* not a traditional JavaFX application
* not based on standard UI components
* not a fixed framework

---

## 🔷 Use Cases

* experimental UI systems
* dynamic dashboards
* research on data-driven design

---

## 🔷 Status

Conceptual / evolving

---

## 🔷 Contributing

You are encouraged to:

* explore the idea
* reinterpret the system
* build your own variation

There is no single correct implementation.

---

## 🔷 Final Thought

This project is not about building UI.

It is about redefining how UI can exist.

> Move logic from code → into data
