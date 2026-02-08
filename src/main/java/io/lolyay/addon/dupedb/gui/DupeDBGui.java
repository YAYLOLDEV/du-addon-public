package io.lolyay.addon.dupedb.gui;

import io.lolyay.addon.dupedb.DupeDBApi;
import io.lolyay.addon.dupedb.dao.ExploitListDao;
import io.lolyay.addon.ui.WGrid;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.systems.Systems;

public class DupeDBGui extends WindowScreen {
    public DupeDBGui() {
        super(GuiThemes.get(), "DupeDB");
    }


    @Override
    public void initWidgets() {

        DupeDBApi dupeDBApi = Systems.get(DupeDBApi.class);
        WGrid grid = add(new WGrid())
            .expandX()
            .padLeft(20)
            .padRight(20)
            .padTop(20)
            .padBottom(20)
            .widget();
        grid.horizontalSpacing = 15;
        grid.verticalSpacing = 15;

        ExploitListDao exploits = dupeDBApi.getRequestor().getExploits();
        for (ExploitListDao.ExploitsItem exploit : exploits.exploitsList()) {
            WTable table = theme.table();
            table.add(theme.label(exploit.name()));
            table.add(theme.button("View")).right();
            grid.add(table);
        }

    }

}
