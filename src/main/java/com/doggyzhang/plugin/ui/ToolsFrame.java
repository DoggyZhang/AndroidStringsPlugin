package com.doggyzhang.plugin.ui;

import com.doggyzhang.plugin.bean.*;
import com.doggyzhang.plugin.checker.*;
import com.doggyzhang.plugin.configs.Configs;
import com.doggyzhang.plugin.translate.*;
import com.doggyzhang.plugin.translate.youdao.*;
import com.doggyzhang.plugin.utils.ExcelUtil;
import com.doggyzhang.plugin.utils.FileUtils;
import com.doggyzhang.plugin.utils.Utils;
import com.doggyzhang.plugin.utils.XmlUtil;
import com.doggyzhang.plugin.widget.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressWindow;
import com.intellij.openapi.project.Project;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;

public class ToolsFrame extends JFrame {
    private static final Logger LOG = Logger.getInstance(ToolsFrame.class);
    private JPanel contentPane;
    private JPanel jBottomPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabPane;
    private JButton btnBrowser;
    private String lastSelectFileDir;
    private JTextField jtfExportSaveFilePath;
    private JButton btnExportSaveFile;
    private JTextField jtWorksheetName;
    private JCheckBox cbForceReplace;
    private JCheckBox jcFormatXml;
    private JComboBox<ComboBoxModelBean> cbExcelFilePath;
    private JComboBox<ComboBoxModelBean> cbMainFolder;
    private JButton btnSelectModule;
    private JButton btnSelectExportModuleFolder;
    private JComboBox<ComboBoxModelBean> cbExportModuleFolder;
    private JCheckBox cbContainLib;
    private JCheckBox jcbExportAll;
    private JList<ComboBoxModelBean> jListTranslateModule;
    private DefaultListSelectionModel jListTranslateSelectionModel;
    private JTextField jTextTranslateAuthKey;
    private JButton jBtnTranslateAll;
    private JCheckBox jCheckBoxTranslateExport;
    private JTextField jTextTranslateExportFolder;
    private JPanel jPanelTranslateLanguage;
    private JCheckBox jCheckTranslateReplaceOld;
    private JButton jButtonSelectAllLanguage;

    private ITranslate translate = new YouDaoTranslate();
    private JFileChooser mOpenFileDialog;
    private JFileChooser mSaveFileDialog;
    private JFileChooser mSelectModuleFileDialog;
    private JFileChooser mSelectExportModuleFileDialog;
    private int selectedPanelIndex;
    private final Project project;
    private final File rootDir;
    private String excelFilePath;
    private String moduleFolderPath;
    private String exportModuleFolderPath;


    public ToolsFrame(File rootDir, Project project, List<String> excelFiles,
                      List<String> moduleFiles) throws HeadlessException {
        super("");
        setContentPane(contentPane);

        getRootPane().setDefaultButton(buttonOK);
        this.project = project;
        this.rootDir = rootDir;
        btnBrowser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mOpenFileDialog == null) {
                    mOpenFileDialog = new JFileChooser();
                }
                FileNameExtensionFilter xlsx = new FileNameExtensionFilter("MS Excel file(*.xlsx;*.xls)",
                        "xlsx", "xls");
                if (lastSelectFileDir != null) {
                    mOpenFileDialog.setCurrentDirectory(new File(lastSelectFileDir));
                } else {
                    mOpenFileDialog.setCurrentDirectory(rootDir);
                }
                mOpenFileDialog.addChoosableFileFilter(xlsx);
                mOpenFileDialog.setAcceptAllFileFilterUsed(false);
                mOpenFileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (mOpenFileDialog.showOpenDialog(ToolsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    String path = mOpenFileDialog.getSelectedFile().getAbsolutePath();
                    if (!path.endsWith(".xlsx") && !path.endsWith(".xls")) {
                        JOptionPane.showMessageDialog(ToolsFrame.this,
                                "输入的文件类型不合法！输入文件必须是xlsx文件！");
                        return;
                    }
                    File selectFile = new File(path);
                    lastSelectFileDir = selectFile.getParent();
                    addComBoxModel(path);
                }
            }
        });
        btnSelectModule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mSelectModuleFileDialog == null) {
                    mSelectModuleFileDialog = new JFileChooser();
                }
                mSelectModuleFileDialog.setCurrentDirectory(rootDir);
                mSelectModuleFileDialog.setAcceptAllFileFilterUsed(false);
                mSelectModuleFileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (mSelectModuleFileDialog.showOpenDialog(ToolsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    String path = mSelectModuleFileDialog.getSelectedFile().getAbsolutePath();
                    addModuleComBoxModel(path);
                }
            }
        });
        btnSelectExportModuleFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mSelectExportModuleFileDialog == null) {
                    mSelectExportModuleFileDialog = new JFileChooser();
                }
                mSelectExportModuleFileDialog.setCurrentDirectory(rootDir);
                mSelectExportModuleFileDialog.setAcceptAllFileFilterUsed(false);
                mSelectExportModuleFileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (mSelectExportModuleFileDialog.showOpenDialog(ToolsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    String path = mSelectExportModuleFileDialog.getSelectedFile().getAbsolutePath();
                    addExportModuleComBoxModel(path);
                }
            }
        });
        PromptSupport.init("您要导入的数据在哪个工作表？如：Sheet0", Color.LIGHT_GRAY,
                null, jtWorksheetName);
        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectedPanelIndex = tabPane.getSelectedIndex();
            }
        });
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (selectedPanelIndex) {
                    case 0:
                        parseExcelToXml();
                        break;
                    case 1:
                        parseStringXmlToExcel();
                        break;
                    case 2:
                        parseAndTransLate();
                        break;
                }
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        btnExportSaveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mSaveFileDialog == null) {
                    mSaveFileDialog = new JFileChooser();
                }
                FileNameExtensionFilter xlsx = new FileNameExtensionFilter("MS Excel file(*.xlsx;*.xls)",
                        "xlsx", "xls");
                mSaveFileDialog.addChoosableFileFilter(xlsx);
                mSaveFileDialog.setAcceptAllFileFilterUsed(false);
                mSaveFileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (mSaveFileDialog.showSaveDialog(ToolsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    String path = mSaveFileDialog.getSelectedFile().getAbsolutePath();
                    jtfExportSaveFilePath.setText(path);
                }
            }
        });
        if (!excelFiles.isEmpty()) {
            ComboBoxListModel myModel = new ComboBoxListModel();
            for (String excelFile : excelFiles) {
                final File file = new File(rootDir, excelFile);
                myModel.addElement(new ComboBoxModelBean(file.getAbsolutePath(), file.getName()));
            }
            cbExcelFilePath.setModel(myModel);
            setSelectExcelFilePath(new ItemEvent(cbExcelFilePath, ItemEvent.ITEM_STATE_CHANGED,
                    null, ItemEvent.SELECTED));
        }
        if (!moduleFiles.isEmpty()) {
            ComboBoxListModel myModel = new ComboBoxListModel();
            ComboBoxListModel exportModel = new ComboBoxListModel();
            for (String moduleFile : moduleFiles) {
                final File file = new File(rootDir, moduleFile);
                final File parentFile = file.getParentFile();
                myModel.addElement(new ComboBoxModelBean(file.getAbsolutePath(), parentFile.getName()));
                exportModel.addElement(new ComboBoxModelBean(parentFile.getAbsolutePath(), parentFile.getName()));
            }
            cbMainFolder.setModel(myModel);
            cbExportModuleFolder.setModel(exportModel);
            setSelectModuleFilePath(new ItemEvent(cbMainFolder, ItemEvent.ITEM_STATE_CHANGED,
                    null, ItemEvent.SELECTED));
            setSelectExcelModuleFilePath(new ItemEvent(cbExportModuleFolder, ItemEvent.ITEM_STATE_CHANGED,
                    null, ItemEvent.SELECTED));
        }
        cbExcelFilePath.addItemListener(this::setSelectExcelFilePath);
        cbMainFolder.addItemListener(this::setSelectModuleFilePath);
        cbExportModuleFolder.addItemListener(this::setSelectExcelModuleFilePath);


        /*
          翻译并补齐
         */
        jTextTranslateAuthKey.setText(translate.getLastAuthKey(project));
//        jTextTranslateAuthKey.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//
//            }
//        });
        jListTranslateModule.setCellRenderer(new ModuleJCheckBox<>());
        ListSelectionModel jListTranslateSelectionModel = new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        };
        jListTranslateSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jListTranslateModule.setSelectionModel(jListTranslateSelectionModel);
        jListTranslateModule.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println(e);
            }
        });

        if (!moduleFiles.isEmpty()) {
            ComboBoxListModel modelList = new ComboBoxListModel();
            for (String moduleFile : moduleFiles) {
                final File file = new File(rootDir, moduleFile);
                final File parentFile = file.getParentFile();
                modelList.addElement(new ComboBoxModelBean(file.getAbsolutePath(), parentFile.getName()));
            }
            jListTranslateModule.setModel(modelList);
        }
        jBtnTranslateAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < jListTranslateModule.getModel().getSize(); i++) {
                    jListTranslateModule.setSelectedIndex(i);
                }
            }
        });
        jTextTranslateExportFolder.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mSaveFileDialog == null) {
                    mSaveFileDialog = new JFileChooser();
                }
                mSaveFileDialog.addChoosableFileFilter(null);
                mSaveFileDialog.setAcceptAllFileFilterUsed(false);
                mSaveFileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (mSaveFileDialog.showSaveDialog(ToolsFrame.this) == JFileChooser.APPROVE_OPTION) {
                    String path = mSaveFileDialog.getSelectedFile().getAbsolutePath();
                    jTextTranslateExportFolder.setText(path);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        jButtonSelectAllLanguage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < jPanelTranslateLanguage.getComponentCount(); i++) {
                    Component component = jPanelTranslateLanguage.getComponent(i);
                    if (component instanceof JCheckBox) {
                        ((JCheckBox) component).setSelected(true);
                    }
                }
            }
        });

        jPanelTranslateLanguage.setLayout(new GridLayout(3, 5, 4, 4));
        for (Language language : Language.values()) {
            JCheckBox checkBox = new JCheckBox(language.getLanguageCode());
            checkBox.setSelected(false);
            jPanelTranslateLanguage.add(checkBox);
        }

        Utils.sizeWindowOnScreen(this, 550, 260);
        pack();
    }

    private void setSelectExcelFilePath(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            ComboBoxModelBean selectedBook = (ComboBoxModelBean) cbExcelFilePath.getSelectedItem();
            if (selectedBook != null) {
                excelFilePath = selectedBook.getOriFilePath();
                System.out.println(MessageFormat.format("{0}:{1}", selectedBook.getShowPath(),
                        selectedBook.getOriFilePath()));
            }
        }
    }

    private void setSelectModuleFilePath(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            ComboBoxModelBean selectedBook = (ComboBoxModelBean) cbMainFolder.getSelectedItem();
            if (selectedBook != null) {
                moduleFolderPath = selectedBook.getOriFilePath();
                System.out.println(MessageFormat.format("{0}:{1}", selectedBook.getShowPath(),
                        selectedBook.getOriFilePath()));
            }
        }
    }

    private void setSelectExcelModuleFilePath(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            ComboBoxModelBean selectedBook = (ComboBoxModelBean) cbExportModuleFolder.getSelectedItem();
            if (selectedBook != null) {
                exportModuleFolderPath = selectedBook.getOriFilePath();
                System.out.println(MessageFormat.format("{0}:{1}", selectedBook.getShowPath(),
                        selectedBook.getOriFilePath()));
            }
        }
    }

    public void addComBoxModel(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        ComboBoxModel<ComboBoxModelBean> comboBoxModel = cbExcelFilePath.getModel();
        ComboBoxListModel model;
        if (comboBoxModel instanceof ComboBoxListModel) {
            model = (ComboBoxListModel) comboBoxModel;
        } else {
            model = new ComboBoxListModel();
        }
        int length = model.getSize();
        boolean isAdd = true;
        for (int i = 0; i < length; i++) {
            final ComboBoxModelBean modelBean = model.getElementAt(i);
            if (filePath.equalsIgnoreCase(modelBean.getOriFilePath())) {
                cbExcelFilePath.setSelectedIndex(i);
                isAdd = false;
                break;
            }
        }
        excelFilePath = filePath;
        if (isAdd) {
            ComboBoxModelBean modelBean = new ComboBoxModelBean(file.getAbsolutePath(), file.getName());
            model.addElement(modelBean);
            cbExcelFilePath.setModel(model);
            cbExcelFilePath.setSelectedItem(modelBean);
        }
    }

    public void addModuleComBoxModel(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        ComboBoxModel<ComboBoxModelBean> comboBoxModel = cbMainFolder.getModel();
        ComboBoxListModel model;
        if (comboBoxModel instanceof ComboBoxListModel) {
            model = (ComboBoxListModel) comboBoxModel;
        } else {
            model = new ComboBoxListModel();
        }
        int length = model.getSize();
        boolean isAdd = true;
        for (int i = 0; i < length; i++) {
            final ComboBoxModelBean modelBean = model.getElementAt(i);
            File oriFile = new File(modelBean.getOriFilePath());
            if (filePath.equalsIgnoreCase(oriFile.getAbsolutePath())
                    || filePath.equalsIgnoreCase(oriFile.getParentFile().getAbsolutePath())) {
                cbMainFolder.setSelectedIndex(i);
                isAdd = false;
                break;
            }
        }
        moduleFolderPath = filePath;
        if (isAdd) {
            ComboBoxModelBean modelBean = new ComboBoxModelBean(file.getAbsolutePath(), file.getName());
            model.addElement(modelBean);
            cbMainFolder.setModel(model);
            cbMainFolder.setSelectedItem(modelBean);
        }
    }

    public void addExportModuleComBoxModel(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        ComboBoxModel<ComboBoxModelBean> comboBoxModel = cbExportModuleFolder.getModel();
        ComboBoxListModel model;
        if (comboBoxModel instanceof ComboBoxListModel) {
            model = (ComboBoxListModel) comboBoxModel;
        } else {
            model = new ComboBoxListModel();
        }
        int length = model.getSize();
        boolean isAdd = true;
        for (int i = 0; i < length; i++) {
            final ComboBoxModelBean modelBean = model.getElementAt(i);
            File oriFile = new File(modelBean.getOriFilePath());
            if (filePath.equalsIgnoreCase(oriFile.getAbsolutePath())
                    || filePath.equalsIgnoreCase(oriFile.getParentFile().getAbsolutePath())) {
                cbExportModuleFolder.setSelectedIndex(i);
                isAdd = false;
                break;
            }
        }
        exportModuleFolderPath = filePath;
        if (isAdd) {
            ComboBoxModelBean modelBean = new ComboBoxModelBean(file.getAbsolutePath(), file.getName());
            model.addElement(modelBean);
            cbExportModuleFolder.setModel(model);
            cbExportModuleFolder.setSelectedItem(modelBean);
        }
    }

    private void parseAndTransLate() {
        String authKey = jTextTranslateAuthKey.getText();
        if (StringUtils.isEmpty(authKey)) {
            showMessageDialog("请输入翻译授权码");
            return;
        }

        int[] selectedIndices = jListTranslateModule.getSelectionModel().getSelectedIndices();
        if (selectedIndices == null || selectedIndices.length == 0) {
            showMessageDialog("请选择翻译的模块");
            return;
        }
        File outputFolder;
        if (jCheckBoxTranslateExport.isSelected()) {
            String exportPath = jTextTranslateExportFolder.getText();
            if (StringUtils.isEmpty(exportPath)) {
                showMessageDialog("导出路径为空, 要选择导出文件夹");
                return;
            }
            outputFolder = new File(exportPath);
            if (!outputFolder.exists()) {
                showMessageDialog("导出文件夹不存在");
                return;
            }
            if (!outputFolder.isDirectory()) {
                showMessageDialog("选择的导出的路径不是文件夹");
                return;
            }
        } else {
            outputFolder = null;
        }

        List<Language> targetLanguages = new ArrayList<>();
        for (int i = 0; i < jPanelTranslateLanguage.getComponentCount(); i++) {
            Component component = jPanelTranslateLanguage.getComponent(i);
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = ((JCheckBox) component);
                if (checkBox.isSelected()) {
                    String languageStr = checkBox.getText();
                    Language targetLanguage = Language.getLanguageBy(languageStr);
                    if (targetLanguage == null) {
                        showMessageDialog("目标语言" + languageStr + "无法识别, 联系DoggyZhang解决");
                        continue;
                    }
                    targetLanguages.add(targetLanguage);
                }
            }
        }

        //替换旧翻译
        boolean replaceOld = jCheckTranslateReplaceOld.isSelected();

        System.out.println("要翻译的目标语言: " + StringUtils.join(targetLanguages, ","));
        Utils.runWithNotification(() -> {
                    ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                    progressIndicator.setIndeterminate(false);

                    for (int i = 0; i < selectedIndices.length; i++) {
                        int index = selectedIndices[i];
                        ComboBoxModelBean modelBean = jListTranslateModule.getModel().getElementAt(index);

                        System.out.println("> 当前处理的模块:" + modelBean.getShowPath());
                        //------------------------------------------------
                        progressIndicator.setText(
                                "> 当前处理的模块:" + modelBean.getShowPath()
                        );
                        progressIndicator.setFraction(0);

                        String currentProgressStr =
                                "(" + (i + 1) + "/" + selectedIndices.length + ")" + modelBean.getShowPath()
                                        + " --> ";
                        File moduleFile = new File(modelBean.getOriFilePath());

                        //------------------------------------------------
                        System.out.println("  解析strings.xml");
                        progressIndicator.setText(
                                currentProgressStr + "解析strings.xml"
                        );
                        progressIndicator.setFraction(0.2);

                        Map<String, List<MultiLanguageBean>> languages = XmlUtil.paresXmlMultiLanguage(moduleFile, true);
                        System.out.println("  当前模块下的语种: " + StringUtils.join(languages.keySet(), ","));

                        //检查是否含有"en"
                        if (!languages.containsKey("en")) {
                            showMessageDialog("模块:" + modelBean.getShowPath() + ", 没有strings.xml文件");
                            return;
                        }

                        //------------------------------------------------
                        System.out.println("  翻译中");
                        progressIndicator.setText(
                                currentProgressStr + "翻译中"
                        );
                        progressIndicator.setFraction(0.5);

                        //以英文翻译为模板来翻译
                        List<MultiLanguageBean> enTranslateList = languages.get("en");
                        List<String> enInputList = enTranslateList.stream().map(new Function<MultiLanguageBean, String>() {
                            @Override
                            public String apply(MultiLanguageBean multiLanguageBean) {
                                return multiLanguageBean.getValue();
                            }
                        }).collect(Collectors.toList());
                        translate.setAuthKey(project, authKey);
                        Map<Language, Map<String, String>> translateResult = translate.translate(
                                enInputList,
                                Language.EN,
                                targetLanguages,
                                new ITranslateProgress() {
                                    @Override
                                    public void onProgressUpdate(String progress) {
                                        progressIndicator.setText(progress);
                                    }

                                    @Override
                                    public void onError(String msg) {
                                        showMessageDialog(msg);
                                    }
                                }
                        );

                        boolean existTranslateError = false;
                        for (Map.Entry<Language, Map<String, String>> translateEntry : translateResult.entrySet()) {
                            if (translateEntry.getValue().isEmpty()) {
                                System.out.println("目标语言: " + translateEntry.getKey().getLanguageCode() + ", 翻译失败, 内容为空");
                                continue;
                            }
                            Language targetLanguage = translateEntry.getKey();
                            if (languages.containsKey(targetLanguage.getLanguageCode())) {
                                //已经有翻译了
                                if (replaceOld) {
                                    //替换旧翻译
                                    List<MultiLanguageBean> newTranslateList = new ArrayList<>();
                                    for (MultiLanguageBean enTranslate : enTranslateList) {
                                        final MultiLanguageBean newTranslateBean = new MultiLanguageBean();
                                        newTranslateBean.setLanguage("");
                                        newTranslateBean.setLanguageCode(targetLanguage.getLanguageCode());
                                        newTranslateBean.setName(enTranslate.getName());

                                        String enValue = enTranslate.getValue();
                                        String targetValue = translateEntry.getValue().get(enValue);
                                        //检查翻译的是否准确(占位符)
                                        long enPlaceHolderCount = XMLPlaceHolderChecker.countPlaceHolder(enValue);
                                        long targetPlaceHolderCount = XMLPlaceHolderChecker.countPlaceHolder(targetValue);
                                        newTranslateBean.setValue(targetValue);
                                        if (enPlaceHolderCount != targetPlaceHolderCount) {
                                            existTranslateError = true;
                                            newTranslateBean.setTranslateError(true);
                                        } else {
                                            newTranslateBean.setTranslateError(false);
                                        }

                                        newTranslateList.add(newTranslateBean);
                                    }
                                    languages.put(targetLanguage.getLanguageCode(), newTranslateList);
                                } else {
                                    //补全缺失部分
                                    List<MultiLanguageBean> oldTranslateList = languages.get(targetLanguage.getLanguageCode());
                                    for (MultiLanguageBean enTranslate : enTranslateList) {
                                        boolean isExist = false;
                                        for (MultiLanguageBean oldTranslate : oldTranslateList) {
                                            if (oldTranslate.getName().equals(enTranslate.getName())) {
                                                isExist = true;
                                                break;
                                            }
                                        }
                                        if (!isExist) {
                                            //旧翻译缺失
                                            final MultiLanguageBean newTranslateBean = new MultiLanguageBean();
                                            newTranslateBean.setLanguage("");
                                            newTranslateBean.setLanguageCode(targetLanguage.getLanguageCode());
                                            newTranslateBean.setName(enTranslate.getName());

                                            String enValue = enTranslate.getValue();
                                            String targetValue = translateEntry.getValue().get(enValue);
                                            //检查翻译的是否准确(占位符)
                                            long enPlaceHolderCount = XMLPlaceHolderChecker.countPlaceHolder(enValue);
                                            long targetPlaceHolderCount = XMLPlaceHolderChecker.countPlaceHolder(targetValue);
                                            newTranslateBean.setValue(targetValue);
                                            if (enPlaceHolderCount != targetPlaceHolderCount) {
                                                existTranslateError = true;
                                                newTranslateBean.setTranslateError(true);
                                            } else {
                                                newTranslateBean.setTranslateError(false);
                                            }

                                            oldTranslateList.add(newTranslateBean);
                                        }
                                    }
                                }
                            } else {
                                //没有该翻译,创建该翻译
                                List<MultiLanguageBean> newTranslateList = new ArrayList<>();
                                for (MultiLanguageBean enTranslate : enTranslateList) {
                                    final MultiLanguageBean newTranslateBean = new MultiLanguageBean();
                                    newTranslateBean.setLanguage("");
                                    newTranslateBean.setLanguageCode(targetLanguage.getLanguageCode());
                                    newTranslateBean.setName(enTranslate.getName());

                                    String enValue = enTranslate.getValue();
                                    String targetValue = translateEntry.getValue().get(enValue);
                                    //检查翻译的是否准确(占位符)
                                    long enPlaceHolderCount = XMLPlaceHolderChecker.countPlaceHolder(enValue);
                                    long targetPlaceHolderCount = XMLPlaceHolderChecker.countPlaceHolder(targetValue);
                                    newTranslateBean.setValue(targetValue);
                                    if (enPlaceHolderCount != targetPlaceHolderCount) {
                                        existTranslateError = true;
                                        newTranslateBean.setTranslateError(true);
                                    } else {
                                        newTranslateBean.setTranslateError(false);
                                    }

                                    newTranslateList.add(newTranslateBean);
                                }
                                languages.put(targetLanguage.getLanguageCode(), newTranslateList);
                            }
                        }
                        System.out.println("  替换(补充)旧文案");
                        progressIndicator.setText(
                                currentProgressStr + "替换(补充)旧文案"
                        );
                        progressIndicator.setFraction(0.8);
                        //替换旧翻译
                        Map<String, List<ElementBean>> replaceData = new HashMap<>();
                        for (Map.Entry<String, List<MultiLanguageBean>> translateData : languages.entrySet()) {
                            if (translateData.getKey().equals("en")) {
                                //英语不纳入最后的替换操作
                                continue;
                            }
                            List<ElementBean> replaceList = translateData.getValue().stream().map(new Function<MultiLanguageBean, ElementBean>() {
                                @Override
                                public ElementBean apply(MultiLanguageBean multiLanguageBean) {
                                    return new ElementBean(
                                            multiLanguageBean.getName(),
                                            multiLanguageBean.getValue(),
                                            multiLanguageBean.isTranslateError()
                                    );
                                }
                            }).collect(Collectors.toList());
                            replaceData.put(translateData.getKey(), replaceList);
                        }
                        if (!replaceData.isEmpty()) {
                            XmlUtil.forceReplace(replaceData, true, moduleFile);
                        }

                        //导出新翻译
                        boolean exportToExcel = jCheckBoxTranslateExport.isSelected() && outputFolder != null;
                        if (exportToExcel) {
                            //------------------------------------------------
                            System.out.println("  导出Excel");
                            progressIndicator.setText(
                                    currentProgressStr + "导出Excel"
                            );
                            progressIndicator.setFraction(0.9);
                            ExcelUtil.generateExcelFile(new File(outputFolder, modelBean.getShowPath() + ".xls"), languages);
                        }

                        //弹窗提示存在翻译错误问题,需要手动纠正
                        if (existTranslateError) {
                            if (exportToExcel) {
                                showMessageDialog("存在一些翻译错误, 搜索XML属性isTranslateError, 然后手动更正, 也可以查看导出的Excel文件");
                            } else {
                                showMessageDialog("存在一些翻译错误, 搜索XML属性isTranslateError, 然后手动更正");
                            }
                        }
                    }
                },
                project,
                Utils.makeProgress("处理中", project, true, false, true));
    }


    /**
     * 扫描解析values.xml，并上传到服务器
     *
     * @author dingpeihua
     * @date 2019/7/29 14:52
     * @version 1.0
     */
    private void parseStringXmlToExcel() {
        String saveFilePath = this.jtfExportSaveFilePath.getText();
        LOG.info("ToolsSettings>>" + saveFilePath);
        if (StringUtils.isEmpty(saveFilePath)) {
            showMessageDialog("存储文件路径不能为空！");
            return;
        }
        boolean containLib = cbContainLib.isSelected();
        if (jcbExportAll.isSelected()) {
            parseAllStringXmlToExcel(containLib, saveFilePath);
            return;
        }

        File moduleFile;
        if (StringUtils.isNotEmpty(exportModuleFolderPath)) {
            moduleFile = new File(exportModuleFolderPath);
        } else {
            moduleFile = new File(rootDir, Configs.PROJECT_APP_FOLDER);
        }

        File file = new File(saveFilePath);
        if (file.isDirectory()) {
            if (StringUtils.isEmpty(exportModuleFolderPath)) {
                String dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
                file = new File(saveFilePath, project.getName() + "-" + dateFormat + ".xls");
            } else {
                file = new File(saveFilePath, moduleFile.getName() + ".xls");
            }
        }

        File outputFile = file;
        Utils.runWithNotification(() -> {
            Map<String, List<MultiLanguageBean>> languages = XmlUtil.paresXmlMultiLanguage(moduleFile, !containLib);
            ExcelUtil.generateExcelFile(outputFile, languages);
            showMessageDialog("生成Excel文件成功！");
        }, project);
    }

    private void parseAllStringXmlToExcel(boolean containLib, String saveFileDir) {
        File dir = new File(saveFileDir);
        if (!dir.isDirectory() || !dir.exists()) {
            showMessageDialog("请选择一个保存目录");
            return;
        }
        ComboBoxModel<ComboBoxModelBean> models = cbExportModuleFolder.getModel();
        showMessageDialog("导出全部, 总共" + models.getSize() + "个");
        Utils.runWithNotification(() -> {

            List<CharCountData> chineseCountList = new ArrayList<>();

            for (int i = 0; i < models.getSize(); i++) {
                ComboBoxModelBean model = models.getElementAt(i);
                File moduleFile;
                if (StringUtils.isNotEmpty(model.getOriFilePath())) {
                    moduleFile = new File(model.getOriFilePath());
                } else {
                    moduleFile = new File(rootDir, Configs.PROJECT_APP_FOLDER);
                }
                File outputFile = new File(saveFileDir, moduleFile.getName() + ".xls");
                Map<String, List<MultiLanguageBean>> languages = XmlUtil.paresXmlMultiLanguage(moduleFile, !containLib);
                if (languages.isEmpty()) {
                    continue;
                }
                ExcelUtil.generateExcelFile(outputFile, languages);
                CharCountData chineseCharCount = collectChineseInfo(moduleFile.getName(), languages);
                if (chineseCharCount.count > 0) {
                    chineseCountList.add(chineseCharCount);
                }
            }

            saveChineseInfo(chineseCountList, saveFileDir);
            showMessageDialog("导出成功");
        }, project);
    }

    private CharCountData collectChineseInfo(String modelName, Map<String, List<MultiLanguageBean>> languages) {
        if (languages == null || languages.isEmpty()) {
            return new CharCountData(modelName, 0);
        }
        //统计中文字数
        List<MultiLanguageBean> zh = languages.get("zh");
        int count = 0;
        if (zh != null) {
            for (MultiLanguageBean bean : zh) {
                String value = bean.getValue();
                count += chineseCount(value);
            }
        }
        return new CharCountData(modelName, count);
    }

    private void saveChineseInfo(List<CharCountData> lines, String saveFileDir) {
        if (lines == null || lines.isEmpty()) {
            showMessageDialog("统计中文字数失败(没有内容)");
            return;
        }
        int allCount = 0;
        for (CharCountData line : lines) {
            allCount += line.count;
        }
        lines.add(0, new CharCountData("全部", allCount));
        List<String> strLines = lines.stream().map(CharCountData::toString).collect(Collectors.toList());
        File output = new File(saveFileDir, "中文字数统计(去除标点符号).txt");
        //输出统计结果
        try {
            if (!output.exists()) {
                output.createNewFile();
            }
            org.apache.commons.io.FileUtils.writeLines(output, strLines);
            showMessageDialog("统计中文字数成功");
        } catch (IOException e) {
            showMessageDialog("统计中文字数失败");
        }
    }

    /**
     * 计算中文字数(不包含标点符号)
     */
    private int chineseCount(String content) {
        if (content == null || content.length() == 0) {
            return 0;
        }
        int count = 0;
        Set<Character> withoutC = new HashSet<>();
        withoutC.add('，');
        withoutC.add('。');
        withoutC.add('！');
        withoutC.add('“');
        withoutC.add('”');
        withoutC.add('？');
        char[] c = content.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (withoutC.contains(c[i])) {
                continue;
            }
            String len = Integer.toBinaryString(c[i]);
            if (len.length() > 8) {
                count++;
            }
        }
        return count;
    }

    /**
     * 解析Excel文件并生成strings.xml
     *
     * @author dingpeihua
     * @date 2019/7/29 14:53
     * @version 1.0
     */
    private void parseExcelToXml() {
        LOG.info("ToolsSettings>>" + excelFilePath);
        if (StringUtils.isEmpty(excelFilePath)) {
            showMessageDialog("文件路径不能为空！");
            return;
        }
        File excelFile = new File(excelFilePath);
        if (!excelFile.exists()) {
            showMessageDialog("文件路径" + excelFilePath + "不存在！");
            return;
        }
        String extension = FilenameUtils.getExtension(excelFile.getName());
        LOG.info("ToolsSettings>>extension:" + extension);
        System.out.println("ToolsSettings>>extension:" + extension);
        Workbook wb = null;
        //根据文件后缀（xls/xlsx）进行判断
        try {
            if ("xls".equals(extension)) {
                wb = new HSSFWorkbook(new FileInputStream(excelFile));
            } else if ("xlsx".equals(extension)) {
                wb = new XSSFWorkbook(excelFile);
            } else {
                showMessageDialog("文件类型错误");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessageDialog("出现错误:" + e.getMessage());
            return;
        }
        String sheetName = jtWorksheetName.getText();
        LOG.info("ToolsSettings>>sheetName: " + sheetName);
        if (StringUtils.isEmpty(sheetName)) {
            sheetName = "Sheet0";
        }
        Workbook finalWb = wb;
        String finalSheet = sheetName;
        boolean isForceReplace = cbForceReplace.isSelected();
        buttonOK.setEnabled(false);
        buttonCancel.setEnabled(false);
        ProgressWindow window = Utils.makeProgress("处理中，请稍后...",
                project, true, false, false);
        Utils.runWithNotification(() -> {
            Sheet sheet = finalWb.getSheet(finalSheet);
            Map<String, List<ElementBean>> excelDatas = ExcelUtil.parseExcelForMap2(sheet);
            boolean isFormat = jcFormatXml.isSelected();
            File moduleFile;
            if (StringUtils.isNotEmpty(moduleFolderPath)) {
                moduleFile = new File(moduleFolderPath);
            } else {
                moduleFile = new File(rootDir, Configs.DEFAULT_PROJECT_MAIN_FOLDER);
            }
            if (isForceReplace) {
                //强制替换，不显示对比列表
                XmlUtil.forceReplace(excelDatas, isFormat, moduleFile);
                showMessageDialog("处理完成！");
            } else {
                TreeNode treeNode = XmlUtil.createTreeNode(moduleFile, excelDatas);
                Utils.invokeLater(() -> {
                    LanguageFrame languageFrame = new LanguageFrame(project, isFormat, treeNode, excelDatas);
                    languageFrame.setVisible(true);
                });
            }
            enabledButton();
        }, project, window);
        window.addStateDelegate(new ProgressIndicatorBase());
    }

    private void showMessageDialog(String message) {
        Utils.showMessageDialog(project, message);
    }

    private void enabledButton() {
        Utils.invokeLater(() -> {
            buttonOK.setEnabled(true);
            buttonCancel.setEnabled(true);
        });
    }

    public static void main(String[] args) {
        File rootDir = new File("");
        List<String> excelFiles = FileUtils.scanFiles(rootDir, Configs.PROJECT_EXCEL_FILE);
        List<String> moduleFiles = FileUtils.scanFolder(rootDir, Configs.PROJECT_MODULE_SRC_FOLDER);
        ToolsFrame dialog = new ToolsFrame(rootDir, null,
                excelFiles, moduleFiles);
        dialog.pack();
        dialog.setVisible(true);
    }
}