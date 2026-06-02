CC      = gcc
CFLAGS  = -Wall -Wextra -Wpedantic -pthread -O2
TARGET  = vm
SRC     = vm.c

.PHONY: all clean rebuild

all: $(TARGET)

$(TARGET): $(SRC)
	$(CC) $(CFLAGS) -o $(TARGET) $(SRC)

clean:
	rm -f $(TARGET)

rebuild: clean all
