package sample;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    public GridPane calendar;
    public Label yearLabel;
    public Label monthLabel;
    public Label dateLabel;
    public BorderPane root;
    public VBox inputScreen;
    public TextField inputField;
    public VBox events;
    public VBox floatings;

    private Integer year = 2016;
    private Integer month = 2;
    private Integer date = 10;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillCalendar();
        root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                back();
            } else {
                inputScreen.setVisible(true);
                root.setMouseTransparent(true);
                root.setEffect(new GaussianBlur());
                root.setDisable(true);
                inputField.requestFocus();
            }
        });

        inputScreen.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                inputField.clear();
                back();
            } else if (e.getCode().equals(KeyCode.ENTER)) {
                System.out.println("Command: " + inputField.getText());
                inputField.clear();
                back();
            }
        });

        events.getChildren().add(new TaskCell("1", "eat with me", "at canteen", new Date()));
        events.getChildren().add(new TaskCell("2", "CS2101", "presentation 01", new Date()));
        events.getChildren().add(new TaskCell("3", "Java is cool", "really?", new Date()));
        floatings.getChildren().add(new TaskCell("4", "Give more", "hi, world", new Date()));

    }

    public void increaseMonth(){
        month++;
        if(month == 13){
            month = 1;
            year++;
        }
        fillCalendar();
    }

    public void decreseMonth(){
        month--;
        if(month == 0){
            month = 12;
            year--;
        }
        fillCalendar();
    }

    public void back(){
        Date dateObj = new Date();
        year = dateObj.getYear() + 1900;
        month = dateObj.getMonth() + 1;
        date = dateObj.getDate();
        fillCalendar();
        inputScreen.setVisible(false);
        root.requestFocus();
        root.setEffect(null);
        root.setMouseTransparent(false);
        root.setDisable(false);
    }

    private void updateCalendarLabels(){
        yearLabel.setText(year.toString());
        dateLabel.setText(date.toString());
        switch(month){
            case 1:
                monthLabel.setText("Jan");
                break;
            case 2:
                monthLabel.setText("Feb");
                break;
            case 3:
                monthLabel.setText("Mar");
                break;
            case 4:
                monthLabel.setText("Apr");
                break;
            case 5:
                monthLabel.setText("May");
                break;
            case 6:
                monthLabel.setText("Jun");
                break;
            case 7:
                monthLabel.setText("Jul");
                break;
            case 8:
                monthLabel.setText("Aug");
                break;
            case 9:
                monthLabel.setText("Sep");
                break;
            case 10:
                monthLabel.setText("Oct");
                break;
            case 11:
                monthLabel.setText("Nov");
                break;
            case 12:
                monthLabel.setText("Dec");
                break;
        }

    }

    private void fillCalendar(){
        String dateStart = "00/01/01";
        String dateStop = year+"/"+month+"/01";

        // Custom date format
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diffDays = (d2.getTime() - d1.getTime()) / (24 * 60 * 60 * 1000);
        int startCol = (int)(diffDays) % 7;

        clearCalendar();

        for(int i = 0; i < 31; i++){
            CalendarDate dateCube = new CalendarDate(i+1);
            dateCube.setDateButton("" + (i+1));
            if(i % 3 != 0)
            dateCube.setEventCountLabel(i % 3);
            calendar.add(dateCube, (startCol + i) % 7, (startCol + i) / 7);
            dateCube.dateButton.setOnAction(e->{
                dateLabel.setText(dateCube.date);
                date = Integer.parseInt(dateLabel.getText());
                fillCalendar();
            });
            dateCube.setId(dateCube.date);
            if(date == i+1){
                dateCube.getStyleClass().add("today");
            }
        }

        updateCalendarLabels();


    }

    private void clearCalendar(){
        calendar.getChildren().clear();
    }

}
