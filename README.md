#Photon Reflection
Photon starts moving to the right from point `(0.50, 0.26)` in a grid lattice with a spherical mirror in each point (`R=1/3`). 
The speed of a photon is `1 point per second`. Where will the photon be located after `20 seconds`?

##Photon reflections and final result##

    (0.500000, 0.260000)
    (0.791407, 0.260000)
    (0.886170, 0.686705)
    (0.162338, -0.708868)
    (0.984961, 0.667006)
    (1.167217, 0.288356)
    (6.866226, 0.694688)
    (7.223030, 0.247727)
    (7.668756, -0.037259)
    (7.045625, -0.669804)
    (6.836904, -0.290707)
    (6.073914, -0.325035)
    (5.166353, -0.711144)
    (5.116817, -0.312194)
    (6.029651, -1.667988)
    (6.666874, -1.011762)
    (5.885223, -0.312950)

    FINAL POINT: 
    (4.094557, -0.360327)

    Covered distance: 
    20.000000


##Result dependency on precision##

    N = 50000 => (+4.094557, -0.360327)
    N = 24    => (+4.094557, -0.360327)
    N = 23    => (+4.094557, -0.360328)
    N = 22    => (+4.094556, -0.360306)
    N = 21    => (+4.094557, -0.360325)
    N = 20    => (+4.094578, -0.361136)
    N = 19    => (+4.094611, -0.362331)
    N = 18    => (+4.094777, -0.368034)
    N = 17    => (+5.645412, +0.906681)
    N = 16    => (+6.468524, -1.785508)
    N = 15    => (+6.551874, +1.408657)
    N = 14    => (+5.776366, -4.158442)


Image below illustrates the problem. Each of the coordinates above is labeled with a number, in order.

![Desmos graph](https://raw.githubusercontent.com/LMesaric/PhotonReflection/master/images/PhotonReflection.png)
