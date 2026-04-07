# Steps to Run the Docker Container

1. Head to your favorite cmd line utility
    -  Linux: Anything really
    - Mac: Anything really
    - Windows: Powershell or WSL
2. Make sure you're in the github repo
3. Run `docker build -t compiler-project .`
4. Run `docker run -it --rm -v "$(pwd):/app" compiler-project`
5. If the docker container doesn't work (You should be able to run ./run.sh), let Ronald know!!!

