# Purity

**Purity** is my _fun-time_ projects **collection**. I usually experiment with math, or try to create some good-looking
images, video and sometime try to create music or number sequence, sometime an image filter and many different things. I
usually just create these and keep the end product somewhere, and forgot where the scr code is (it's in scratch, but by what
name). So, here's my all fun-time projects.

### Purity Direction

Purity direction (PD) specifies how the project is make, result data can be created from code (left) or result data is
totally human made (right).

| Direction | Symbol | Meaning                                                                         |
|-----------|--------|---------------------------------------------------------------------------------|
| LEFT PURE | i-     | Totally programmable, Pure and Universal                                        |
| LEFT      | i      | Totally programmable                                                            |
| LEFT MID  | i+     | Programmable but post edits                                                     |
| CENTER    | 0      | Pure Mix of computable and human-only interactions (or unknown for some reason) |
| RIGHT MID | R-     | Human-Made but computer effects later                                           |
| RIGHT     | R      | Totally Human-Made.                                                             |

### Type

There are many type of projects: image, video, sound, number sequence, functions, 3d_models, etc. The
[ProjectType.java](src/main/java/com/ri/meta/ProjectType.java) class gives a very _pure_ way to specify the type of project.

### Unique Locators

A project can be uniquely located by its category, id and name:

- Category & ID: 4 byte value 2 byte for category and 2 byte for ID, may point to several variants/formats at once. It will 
  point to all project with same src or which are similar. Like a random noise generator can generate multiple images or an 
  android vector can be rasterised to a PNG image.

- Name: Simple, Human Readable Name (sometime not readable), can distinguish between different variants/formats. If it's a 
  LEFT PD project, the name must points to the source, or it shall be a text file with src code or src code location. Later 
  info can give parameters or file format or post effects etc.

## Nomenclature

At the end of the day, the naming looks like this

    ([E(Fd1n1)i] <00> 37) Bxn_Bqa_C6 i32

where

- `E(Fd1n1)` mean an easy-edit (E) function (F) of _sound_ (D1) from _int_ (n1)
- `i` is PD, LEFT, means Totally Programmable
- `<00>` category 00 (**VOID**)
- `37` refers to the id
- `Bxn_Bqa_C6 i32` is name (where Bxn_Bqa_C6 is base project name and i32 mean function's value at i=32)

and for URL's

    ([E(Fd1n1)i] 00-37) Bxn_Bqa_C6 i32.wav

## Rili
Since I add every successful independent project to the purity project list, 
it can also include projects from my _rili_ project (and that's private) So,
ignore them.

---

# Pure?

I use the word `Pure~` or `Purity~` to refer to the fact that it feel very nice to me (and sometime to reflect that the
thing is universal, simple and easy to understand)
