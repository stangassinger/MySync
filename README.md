# MySync
Syncing all data in Picture folder from Smartphone to PC


  For the transfer over ssh a private and public key is needed
  Generate it on the linux command line with:
  ssh-keygen -t rsa -b 4096
  put the public-key and private-key into Conf.java file
  the private key must have a \n on every end of line in
  the Conf.java file.
  then copy the public key to the server (PC) into
  ~/.ssh directory and then
  cat id_rsa.pub >> authorized_keys
  
  TBD:
  there should be a similar instuction for windows PC.
  
 

