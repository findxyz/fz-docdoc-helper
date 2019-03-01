package xyz.fz.docdoc.helper;

import xyz.fz.docdoc.helper.controller.DocController;
import xyz.fz.docdoc.helper.form.MainFrame;

public class Application {

    public static void main(String[] args) {

        DocController docController = new DocController();

        MainFrame frame = new MainFrame(docController);
        frame.setVisible(true);

        docController.initScheduleCheck();
    }

}
