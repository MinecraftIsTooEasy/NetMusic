package com.github.tartaricacid.netmusic.client.network;

import moddedmite.rustedironcore.network.Network;
import moddedmite.rustedironcore.network.Packet;

public class ClientNetWorkHandler {

    public static void sendToServer(Packet packet) {
        Network.sendToServer(packet);
    }
}