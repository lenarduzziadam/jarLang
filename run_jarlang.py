#!/usr/bin/env python3
import os
import sys
import subprocess


DOCKER_IMAGE = "jarlang-runner:latest"
PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
JAR_FILE = os.path.join(PROJECT_ROOT, "JarlangRunner", "jarlang.jar")


# Build the docker image if not present
def build_image():
    print("Summoning the Docker cauldron for Jarlang...")
    subprocess.run([
        "docker", "build", "-t", DOCKER_IMAGE, "."
    ], check=True)


def image_exists():
    result = subprocess.run([
        "docker", "images", "-q", DOCKER_IMAGE
    ], stdout=subprocess.PIPE)
    return bool(result.stdout.strip())


def print_medieval_help():
    print("\n=== Welcome to the Jarlang Shell! ===")
    print("REGALLLI! Prepare to wield your code like a true JarKnight!")
    print("\nKnightly Commandments:")
    print("  summon \"file.vase\"   # Import a scroll of wisdom")
    print("  wield x 10              # Wield a variable with valor")
    print("  vow y 42                # Declare a sacred constant")
    print("  sacred z \"legend\"      # Declare a truly sacred variable")
    print("  lest x < 5 {...}        # Repeat whilst the quest endures (while loop)")
    print("  endure ...              # For-loop, for the persistent knight")
    print("  forge fn(a,b) ...       # Forge a function in the fires of code")
    print("  chant \"Hello!\"         # Chant your message to the realm")
    print("  !run file.vase          # Run a scroll in the shell")
    print("  q!                      # Retreat from the shell")
    print("===============================\n")


def run_jarlang_shell():
    print_medieval_help()
    print("Summoning the Jarlang shell in Docker...")
    subprocess.run([
        "docker", "run", "-it", "-v",
        f"{PROJECT_ROOT}:/jarlang",
        DOCKER_IMAGE,
        "bash", "-c", "cd /jarlang/JarlangRunner && java -jar jarlang.jar"
    ])


def run_jarlang_file(filename):
    print_medieval_help()
    print(f"Running {filename} in Jarlang Docker...")
    subprocess.run([
        "docker", "run", "-it", "-v",
        f"{PROJECT_ROOT}:/jarlang",
        DOCKER_IMAGE,
        "bash", "-c", f"cd /jarlang/JarlangRunner && java -jar jarlang.jar && !run {filename}"
    ])

def main():
    if not image_exists():
        build_image()
    if len(sys.argv) == 1:
        run_jarlang_shell()
    else:
        run_jarlang_file(sys.argv[1])

if __name__ == "__main__":
    main()
