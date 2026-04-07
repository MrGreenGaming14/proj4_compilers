# 1. Use the Java SDK base
FROM eclipse-temurin:21-jdk

# 2. Install 'make' and any other build tools needed for your scripts
RUN apt-get update && apt-get install -y \
    make \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 3. Set the working directory
WORKDIR /app

# 4. Execute your specific script
# This assumes your script is named 'run.sh'
CMD ["/bin/bash"]
