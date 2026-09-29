ItemEvents.tooltip(event => {
    event.addAdvanced('kubejs:mineral_extractor', (item, advanced, text) => {
        let nbt = item.nbt;

        // 如果没有 NBT 或者里面没有存储记录，显示提示
        if (!nbt || !nbt.contains('storedRecords') || nbt.getList('storedRecords', 10).size() === 0) {
            text.add(Text.yellow('状态: 空'));
            text.add(Text.gray('请手持岩芯样本(副手)并右键提取器来记录'));
            return;
        }

        let records = nbt.getList('storedRecords', 10);
        text.add(Text.green(`已存储的矿脉数量: ${records.size()} 条`));
        text.add(Text.gray('-----------------------'));

        // 循环遍历每一条记录，动态拼装到 Tooltip 中
        records.forEach(record => {
            let mineral = record.getString('mineral');
            let saturation = record.getDouble('saturation');
            let depletion = record.getInt('depletion');
            let dim = record.getString('dimension');
            let x = record.getInt('originalX');
            let z = record.getInt('originalZ');

            // 格式化显示百分比或饱和度
            let satPercent = (saturation * 100).toFixed(0);
            // 本地化键：矿脉 ID 形如 "kubejs:mineral/copper_tin_rich"，取最后一段作为翻译键后缀
            let mineralNameParts = mineral.split('/');
            let mineralKey = `desc.immersiveengineering.info.mineral.${mineralNameParts[mineralNameParts.length - 1]}`;
            let translatedMineral = Text.translate(mineralKey);
            // 添加每一条矿脉的详细信息行
            text.add(Text.gold("• 矿脉: ").append(translatedMineral));
            text.add(Text.darkGray(`  维度: ${dim} (${x}, ${z})`));
            text.add(Text.darkGray(`  饱和度: ${satPercent}% | 耗竭度: ${depletion}`));
        });
        
        text.add(Text.gray('-----------------------'));
        text.add(Text.yellow('右键随机部署一条矿脉'));
    });
});
