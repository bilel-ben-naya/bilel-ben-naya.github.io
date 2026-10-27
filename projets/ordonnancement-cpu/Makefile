CC=gcc
CFLAGS=-Wall -g
SOURCES=src/main.c src/scheduler.c
OBJECTS=$(SOURCES:.c=.o)
EXEC=simulateur

all: $(EXEC)

$(EXEC): $(OBJECTS)
    $(CC) $(OBJECTS) -o $(EXEC)

clean:
    rm -f $(OBJECTS) $(EXEC)