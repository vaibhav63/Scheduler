package Vaibhav;

import Vaibhav.dataModel.TodoData;
import Vaibhav.dataModel.TodoItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    private List<TodoItem> todoItems;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    ContextMenu contextMenu;
    @FXML
    private ToggleButton toggle;
    FilteredList<TodoItem> filteredList;

    public void initialize() {

        contextMenu=new ContextMenu();
        MenuItem delete=new MenuItem("delete");
        contextMenu.getItems().addAll(delete);
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item=todoListView.getSelectionModel().getSelectedItem();
                remove(item);
            }
        });

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                ListCell<TodoItem> cell=new ListCell<>(){
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(item.getShortDescription());
                            if(item.getDeadline().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            }
                        }
                    }
                };cell.emptyProperty().addListener(
                        (obs,wasEmpty,isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(contextMenu);
                            }
                        }

                );



                return cell;
            }
        });

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if(newValue != null) {
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy"); // "d M yy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });
        SortedList<TodoItem> sortedList=new SortedList<TodoItem>(TodoData.getInstance().getTodoItems(), new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });
       filteredList=new FilteredList<TodoItem>(sortedList, new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        });
        todoListView.setItems(filteredList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();


    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch(IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
           TodoItem newitem= controller.processResults();
          //  todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
            todoListView.getSelectionModel().select(newitem);
            System.out.println("OK pressed");
        } else {
            System.out.println("Cancel pressed");
        }


    }

    @FXML
    public void handleClickListView() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());
    }
    public void remove (TodoItem item){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("detele item");
        alert.setHeaderText("delete"+item.getShortDescription());
        alert.setContentText("press ok to delete, otherwise cancel ");
        Optional<ButtonType> result= alert.showAndWait();
        if(result.isPresent()&& result.get()==ButtonType.OK){
            TodoData.getInstance().removeitem(item);

        }
    }
    public void keydelete(javafx.scene.input.KeyEvent event){
        TodoItem item=todoListView.getSelectionModel().getSelectedItem();
        if(item!=null){
            if(event.getCode().equals(KeyCode.DELETE)){
                remove(item);
            }
        }
    }
    public void togglehandle(){

        if(toggle.isSelected()){
          filteredList.setPredicate(new Predicate<TodoItem>() {
              @Override
              public boolean test(TodoItem item) {
                  return (item.getDeadline().equals(LocalDate.now()));
              }
          });
        }else{
            filteredList.setPredicate(new Predicate<TodoItem>() {
                @Override
                public boolean test(TodoItem item) {
                    return true;
                }
            });
        }
    }

}
