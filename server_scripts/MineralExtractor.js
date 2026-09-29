const EXTRACTOR_ID = 'kubejs:mineral_extractor';
const CORE_SAMPLE_ID = 'immersiveengineering:coresample';
const RECORD_LIST_KEY = 'storedRecords';

const ListTag = Java.loadClass('net.minecraft.nbt.ListTag');

// 获取提取器中的记录列表，不存在时创建
function getOrCreateRecordList(item) {
    let tag = item.orCreateTag;
    if (tag.contains(RECORD_LIST_KEY, 9)) return tag.getList(RECORD_LIST_KEY, 10);

    let list = new ListTag();
    tag.put(RECORD_LIST_KEY, list);
    return list;
}

// 获取提取器中的记录列表，不存在时返回 null
function getRecordList(item) {
    let tag = item.nbt;
    if (!tag || !tag.contains(RECORD_LIST_KEY, 9)) return null;
    return tag.getList(RECORD_LIST_KEY, 10);
}

// 获取矿脉翻译名称
function getTranslatedMineralName(mineralId) {
    let parts = mineralId.split('/');
    let name = parts[parts.length - 1];
    return Text.translate(`desc.immersiveengineering.info.mineral.${name}`);
}

ItemEvents.rightClicked(EXTRACTOR_ID, event => {
    let player = event.player;
    let item = event.item;
    let offhand = player.offHandItem;

    // 副手有岩芯样本：记录样本中的全部矿脉
    if (offhand.id === CORE_SAMPLE_ID) {
        let sampleNBT = offhand.nbt;

        if (!sampleNBT) {
            player.tell(Text.red('岩芯样本没有 NBT 数据。'));
            event.cancel();
            return;
        }

        if (!sampleNBT.contains('mineralInfo', 9)) {
            player.tell(Text.yellow('岩芯样本中没有矿脉信息。'));
            event.cancel();
            return;
        }

        let mineralList = sampleNBT.getList('mineralInfo', 10);
        if (mineralList.size() <= 0) {
            player.tell(Text.yellow('岩芯样本中的矿脉列表为空。'));
            event.cancel();
            return;
        }

        let dimension = sampleNBT.getString('dimension');
        let originalX = sampleNBT.getInt('x');
        let originalZ = sampleNBT.getInt('z');
        let storedRecords = getOrCreateRecordList(item);

        for (let i = 0; i < mineralList.size(); i++) {
            let record = mineralList.getCompound(i).copy();
            record.putString('dimension', dimension);
            record.putInt('originalX', originalX);
            record.putInt('originalZ', originalZ);
            storedRecords.add(record);
            player.runCommand(`execute in ${dimension} run ie mineral setDepletion 38400 ${originalX} ${originalZ}`);
        }

        // 记录完成后清空岩芯样本 NBT
        offhand.setNbt({});

        player.tell(Text.green(`已从岩芯样本中提取 ${mineralList.size()} 条矿脉记录。`));
        event.cancel();
        return;
    }

    // 没有岩芯样本：随机部署一条记录
    let records = getRecordList(item);

    if (!records || records.size() <= 0) {
        player.tell(Text.yellow('矿脉提取器中没有记录过任何矿脉。请在副手手持岩芯样本进行记录。'));
        event.cancel();
        return;
    }

    let randomIndex = Math.floor(Math.random() * records.size());
    let record = records.getCompound(randomIndex);

    let mineral = record.getString('mineral');
    let saturation = record.getDouble('saturation');
    let depletion = record.getInt('depletion');
    let originalX = record.getInt('originalX');
    let originalZ = record.getInt('originalZ');
    let recordedDimension = record.getString('dimension');
    let translatedMineral = getTranslatedMineralName(mineral);

    player.tell(`尝试部署矿脉：${translatedMineral}，成功概率：${(saturation * 100).toFixed(1)}%`);

    // 按饱和度判定是否成功
    if (Math.random() > saturation) {
        player.tell(Text.red('矿脉部署失败。'));
        records.remove(randomIndex);
        event.cancel();
        return;
    }

    // 在玩家当前维度放置矿脉
    player.runCommandSilent(`ie mineral put "${mineral}" 16 ~ ~`);

    // 重置原矿脉位置的 depletion
    player.runCommandSilent(`execute in ${recordedDimension} run ie mineral setDepletion 38400 ${originalX} ${originalZ}`);

    // 恢复部署后矿脉记录的 depletion
    if (depletion !== 0) player.runCommandSilent(`ie mineral setDepletion ${depletion} ~ ~`);

    records.remove(randomIndex);
    player.tell(Text.green(`成功在脚下部署矿脉：${translatedMineral}`));
    event.cancel();
});