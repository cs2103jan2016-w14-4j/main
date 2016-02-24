package sample;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Created by ruomu on 2/23/16.
 */
public class CalendarDate extends Region {
    private HBox layout;
    Button dateButton;
    private Label padding;
    private Label eventCountLabel;
    String date;

    CalendarDate(Integer date){
        this.date = date.toString();
        layout = new HBox();
        dateButton = new Button();
        eventCountLabel = new Label();
        padding = new Label();
        padding.textProperty().bind(eventCountLabel.textProperty());
        padding.visibleProperty().setValue(false);

        this.getChildren().add(layout);

        layout.getChildren().addAll(eventCountLabel, dateButton,padding);

        eventCountLabel.textProperty().addListener((v, oldValue, newValue) -> {
            if(newValue.length() != 0){
                eventCountLabel.setVisible(true);
            }else{
                eventCountLabel.setVisible(false);
            }
        });

        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPrefWidth(50);

        dateButton.getStyleClass().add("datelabel");
        eventCountLabel.getStyleClass().add("eventlabel");

    }

    public void setDateButton(String date){
        dateButton.setText(date);
    }

    public void setEventCountLabel(int number){
        eventCountLabel.setText("" + number);
    }
}
