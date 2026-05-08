package com.github.tartaricacid.netmusic.creativetab;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import huix.glacier.api.extension.creativetab.GlacierCreativeTabs;
import net.minecraft.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetMusicCreativeTab extends GlacierCreativeTabs {

    public static final NetMusicCreativeTab TAB = new NetMusicCreativeTab();

    public NetMusicCreativeTab() {
        super("NetMusic");
    }

    public int getTabIconItemIndex() {
        return InitBlocks.MUSIC_PLAYER.blockID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void displayAllReleventItems(List itemList) {
        super.displayAllReleventItems(itemList);

        if (InitItems.MUSIC_CD == null) {
            return;
        }

        Set<String> addedSongUrls = new HashSet<String>();
        addMusicDiscs(itemList, BuiltinMusicDiscs.getSongs(), addedSongUrls);

        try {
            MusicListManage.loadConfigSongs();
            addMusicDiscs(itemList, MusicListManage.SONGS, addedSongUrls);
        } catch (IOException e) {
            com.github.tartaricacid.netmusic.NetMusic.LOGGER.warn("Failed to load configured music discs for creative tab", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addMusicDiscs(List itemList, List<ItemMusicCD.SongInfo> songs, Set<String> addedSongUrls) {
        if (songs == null || songs.isEmpty()) {
            return;
        }
        for (ItemMusicCD.SongInfo info : songs) {
            if (info == null || StringUtils.isBlank(info.songUrl)) {
                continue;
            }
            String key = info.songUrl.trim().toLowerCase();
            if (!addedSongUrls.add(key)) {
                continue;
            }
            ItemStack stack = new ItemStack(InitItems.MUSIC_CD);
            ItemMusicCD.setSongInfo(info, stack);
            itemList.add(stack);
        }
    }
}
