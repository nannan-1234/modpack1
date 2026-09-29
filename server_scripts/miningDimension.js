// 采矿维度：维度间传送 + 仅采矿维度内允许生存玩家飞行
const MINING_DIMENSION_ID = 'miningdim:mining';
const OVERWORLD_DIMENSION_ID = 'minecraft:overworld';
const TELEPORTER_ID = 'kubejs:mining_dimension_teleporter';

// 采矿维度顶层泥土在 Y=200，传送到 203 避免卡进方块
const MINING_ENTRY_Y = 203;
// 玩家 persistentData 中的飞行授权标记与主世界回程坐标
const FLIGHT_GRANTED_KEY = 'kubejs_mining_dim_flight';
const RETURN_KEY = 'kubejs_mining_return_';
const NBT_DOUBLE = 6;

function getDimensionId(level) {
    return level.dimension.toString();
}

function hasReturnPoint(tag) {
    return tag.contains(RETURN_KEY + 'x', NBT_DOUBLE);
}

ItemEvents.rightClicked(TELEPORTER_ID, event => {
    const player = event.player;
    const dimension = getDimensionId(player.level);

    if (dimension === OVERWORLD_DIMENSION_ID) {
        // 记录主世界当前位置，返回时回到原地附近
        const tag = player.persistentData;
        tag.putDouble(RETURN_KEY + 'x', player.x);
        tag.putDouble(RETURN_KEY + 'y', player.y);
        tag.putDouble(RETURN_KEY + 'z', player.z);
        tag.putDouble(RETURN_KEY + 'yaw', player.yaw);
        tag.putDouble(RETURN_KEY + 'pitch', player.pitch);

        player.tell(Text.green('正在前往采矿维度…'));
        player.teleportTo(
            MINING_DIMENSION_ID,
            player.x,
            MINING_ENTRY_Y,
            player.z,
            player.yaw,
            player.pitch
        );
        event.cancel();
    } else if (dimension === MINING_DIMENSION_ID) {
        const tag = player.persistentData;
        let x;
        let y;
        let z;
        let yaw = 0;
        let pitch = 0;

        if (hasReturnPoint(tag)) {
            x = tag.getDouble(RETURN_KEY + 'x');
            // 记录时玩家站在地面上，+1 保证返回时位于方块上方
            y = tag.getDouble(RETURN_KEY + 'y') + 1;
            z = tag.getDouble(RETURN_KEY + 'z');
            yaw = tag.getDouble(RETURN_KEY + 'yaw');
            pitch = tag.getDouble(RETURN_KEY + 'pitch');
        } else {
            const spawn = player.server.overworld().sharedSpawnPos;
            x = spawn.getX() + 0.5;
            y = spawn.getY() + 1;
            z = spawn.getZ() + 0.5;
        }

        player.tell(Text.green('正在返回主世界…'));
        player.teleportTo(OVERWORLD_DIMENSION_ID, x, y, z, yaw, pitch);
        event.cancel();
    } else {
        player.tell(Text.yellow('该传送石只能在主世界与采矿维度之间使用。'));
        event.cancel();
    }
});

PlayerEvents.tick(event => {
    const player = event.player;
    // 自动机械使用的假玩家不需要飞行授权
    if (player.isFake()) return;

    const tag = player.persistentData;
    const inMiningDimension = getDimensionId(player.level) === MINING_DIMENSION_ID;
    const grantedByThisScript = tag.getBoolean(FLIGHT_GRANTED_KEY);

    if (inMiningDimension) {
        if (!player.abilities.mayfly) {
            // 只标记由本脚本授予的飞行，避免抢走其他装备/模组已有的飞行
            player.abilities.mayfly = true;
            tag.putBoolean(FLIGHT_GRANTED_KEY, true);
            player.onUpdateAbilities();
        }
    } else if (grantedByThisScript) {
        // 离开采矿维度后收回本脚本授予的飞行；创造模式本身不受影响
        if (!player.isCreative() && player.abilities.mayfly) {
            player.abilities.mayfly = false;
            player.abilities.flying = false;
            player.onUpdateAbilities();
        }
        tag.remove(FLIGHT_GRANTED_KEY);
    }
});
