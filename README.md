# Docker Hub Upload Speed Test

This repository provides three ways to test upload speed to Docker Hub
   1. Using Java Google HTTP Client.
   2. Using Java Apache HttpClient.
   3. Using Bash.

## Uploading random 40MB to Docker Hub using Google HTTP Client

   1. Update Docker Hub account and repository information in [`GoogleHttpClientMain.java`](src/main/java/GoogleHttpClientMain.java).
   2. Compile first.
      ```
      $ mvn compile
   3. Run.
      ```
      $ mvn exec:java -Dexec.mainClass=GoogleHttpClientMain
      ```

## Uploading random 40MB to Docker Hub using Apache HttpClient:

   1. Update Docker Hub account and repository information in [`ApacheHttpClientMain.java`](src/main/java/ApacheHttpClientMain.java).
   2. Compile first.
      ```
      $ mvn compile
   3. Run.
      ```
      $ mvn exec:java -Dexec.mainClass=ApacheHttpClientMain
      ```

## Uploading using Bash:

   1. Run the [`push_blob_test.sh`](push_blob_test.sh) script and follow the prompts.
   
      Example output:
      ```
      Enter path to a large file to upload.
      Note you can easily generate a 40mb file with the following command:
          $ dd bs=1M count=40 < /dev/urandom > 40mb.file
      Path? ./40mb.file
      Your Docker Hub account? francium25
      Your Docker Hub password?
      Your Docker Hub target repo (e.g., myaccount/myrepo)? francium25/test
      Verbose curl logging (y/[N])?
        % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                       Dload  Upload   Total   Spent    Left  Speed
      100  4297    0  4297    0     0  28456      0 --:--:-- --:--:-- --:--:-- 28456
 
      >>> Got token (don't reveal this in public): eyJhbG...
 
        % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                       Dload  Upload   Total   Spent    Left  Speed
        0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0
 
      >>> Got push location: https://registry-1.docker.io/v2/francium25/test/blobs/uploads/b7fba65e-9b1c-4ca9-915d-a0f378550df4?_state=_ZzacFZa7VckJYV3aijBIM2ZDjfIPJ0ytY13xkKTx6J7Ik5hbWUiOiJmcmFuY2l1bTI1L3Rlc3QiLCJVVUlEIjoiYjdmYmE2NWUtOWIxYy00Y2E5LTkxNWQtYTBmMzc4NTUwZGY0IiwiT2Zmc2V0IjowLCJTdGFydGVkQXQiOiIyMDIwLTAyLTA0VDE2OjU2OjM3LjYzODY5MTQyWiJ9
 
      Now pushing a BLOb: ./40mb.file
 
 
      real    0m1.885s
      user    0m0.068s
      sys     0m0.077s
      ```
