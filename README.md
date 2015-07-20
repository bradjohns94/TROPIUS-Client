# TRIOPIUS-Client

TROPIUS-Client is a scala-based RESTFUL API designed to be run on machines to be used as hosts to the
TROPIUS home-automation system.</br>
Current Features include:

  - Spotify Track/Album/Artist playback
  - Spotify Play/Pause/Next functionality

TROPIUS-Client is only currently available for Ubuntu and similar Linux systems, but ideally this should change soon.

### Installation

TROPIUS-Client comes with an installation script which will create an assembly JAR file, move it to $HOME/.tropius/ and create an upstart startup task to execute the JAR. To run the install, you can use the following commands
```sh
$ git clone https://github.com/BradJohns94/TROPIUS-Client.git
$ cd TROPIUS-Client/scripts
$ chmod +x ubuntu_install.sh
$ ./ubuntu_install.sh
```

### Licensing

TROPIUS, TROPIUS-Client, and all other TROPIUS-related repositories have literally no licensing. One of these days I should give them the GPL or something, but if you really want to use these, please do. Give me credit if you want, but it really doesn't matter much.