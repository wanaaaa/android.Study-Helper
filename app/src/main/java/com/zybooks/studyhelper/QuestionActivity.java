package com.zybooks.studyhelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

import com.google.android.material.snackbar.Snackbar;


public class QuestionActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "com.zybooks.studyhelper.subject_id";

    private StudyDatabase mStudyDb;
    private long mSubjectId;
    private List<Question> mQuestionList;
    private TextView mAnswerLabel;
    private TextView mAnswerText;
    private Button mAnswerButton;
    private TextView mQuestionText;
    private int mCurrentQuestionIndex;
    private ViewGroup mShowQuestionLayout;
    private ViewGroup mNoQuestionLayout;

    private Question mDeletedQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // SubjectActivity should provide the subject ID of the questions to display
        Intent intent = getIntent();
        mSubjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, 0);

        // Get all questions for this subject
//        mStudyDb = StudyDatabase.getInstance();
//        mQuestionList = mStudyDb.getQuestions(mSubjectId);
        mStudyDb = StudyDatabase.getInstance(getApplicationContext());
        mQuestionList = mStudyDb.questionDao().getQuestions(mSubjectId);


        mQuestionText = findViewById(R.id.question_text_view);
        mAnswerLabel = findViewById(R.id.answer_label_text_view);
        mAnswerText = findViewById(R.id.answer_text_view);
        mAnswerButton = findViewById(R.id.answer_button);
        mShowQuestionLayout = findViewById(R.id.show_question_layout);
        mNoQuestionLayout = findViewById(R.id.no_question_layout);

        // Show first question
        showQuestion(0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mQuestionList.size() == 0) {
            updateAppBarTitle();
            displayQuestion(false);
        }
        else {
            displayQuestion(true);
            toggleAnswerVisibility();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.question_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //  Determine which app bar item was chosen
        if (item.getItemId() == R.id.previous) {
            showQuestion(mCurrentQuestionIndex - 1);
            return true;
        }
        else if (item.getItemId() == R.id.next) {
            showQuestion(mCurrentQuestionIndex + 1);
            return true;
        }
        else if (item.getItemId() == R.id.add) {
            addQuestion();
            return true;
        }
        else if (item.getItemId() == R.id.edit) {
            editQuestion();
            return true;
        }
        else if (item.getItemId() == R.id.delete) {
            deleteQuestion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addQuestionButtonClick(View view) {
        addQuestion();
    }

    public void answerButtonClick(View view) {
        toggleAnswerVisibility();
    }

    private void displayQuestion(boolean display) {
        if (display) {
            mShowQuestionLayout.setVisibility(View.VISIBLE);
            mNoQuestionLayout.setVisibility(View.GONE);
        }
        else {
            mShowQuestionLayout.setVisibility(View.GONE);
            mNoQuestionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateAppBarTitle() {

        // Display subject and number of questions in app bar
        //Subject subject = mStudyDb.getSubject(mSubjectId);
        Subject subject = mStudyDb.subjectDao().getSubject(mSubjectId);
        String title = getResources().getString(R.string.question_number,
                subject.getText(), mCurrentQuestionIndex + 1, mQuestionList.size());
        setTitle(title);
    }

    private void addQuestion() {
        // TODO: Add question
        Intent intent = new Intent(this, QuestionEditActivity.class);
        intent.putExtra(QuestionEditActivity.EXTRA_SUBJECT_ID, mSubjectId);
        mAddQuestionResultLauncher.launch(intent);

    }

    private final ActivityResultLauncher<Intent> mAddQuestionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            long questionId = data.getLongExtra(QuestionEditActivity.EXTRA_QUESTION_ID, -1);
                            Question newQuestion = mStudyDb.questionDao().getQuestion(questionId);

                            // Add newly created question to the question list and show it
                            mQuestionList.add(newQuestion);
                            showQuestion(mQuestionList.size() - 1);

                            // Change layout in case this is the first question
                            displayQuestion(true);

                            Toast.makeText(QuestionActivity.this, R.string.question_added, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


    private void editQuestion() {
        // TODO: Edit question
        if (mCurrentQuestionIndex >= 0) {
            Intent intent = new Intent(this, QuestionEditActivity.class);
            long questionId = mQuestionList.get(mCurrentQuestionIndex).getId();
            intent.putExtra(QuestionEditActivity.EXTRA_QUESTION_ID, questionId);
            mEditQuestionResultLauncher.launch(intent);
        }
    }
    private final ActivityResultLauncher<Intent> mEditQuestionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            // Get updated question
                            long questionId = data.getLongExtra(QuestionEditActivity.EXTRA_QUESTION_ID, -1);
                            Question updatedQuestion = mStudyDb.questionDao().getQuestion(questionId);

                            // Replace current question in question list with updated question
                            Question currentQuestion = mQuestionList.get(mCurrentQuestionIndex);
                            currentQuestion.setText(updatedQuestion.getText());
                            currentQuestion.setAnswer(updatedQuestion.getAnswer());
                            showQuestion(mCurrentQuestionIndex);

                            Toast.makeText(QuestionActivity.this, R.string.question_updated, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private void deleteQuestion() {
        // TODO: Delete question
        if (mCurrentQuestionIndex >= 0) {
            Question question = mQuestionList.get(mCurrentQuestionIndex);
            mStudyDb.questionDao().deleteQuestion(question);
            mQuestionList.remove(mCurrentQuestionIndex);

            // Save question in case user wants to undo delete
            mDeletedQuestion = question;

            if (mQuestionList.isEmpty()) {
                // No questions left to show
                mCurrentQuestionIndex = -1;
                updateAppBarTitle();
                displayQuestion(false);
            }
            else {
                showQuestion(mCurrentQuestionIndex);
            }

            // Show delete message with Undo button
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
                    R.string.question_deleted, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, v -> {
                // Add question back with a new auto-increment id
                mDeletedQuestion.setId(0);
                mStudyDb.questionDao().insertQuestion(mDeletedQuestion);

                // Add question back to list of questions, and display it
                mQuestionList.add(mDeletedQuestion);
                showQuestion(mQuestionList.size() - 1);
                displayQuestion(true);
            });
            snackbar.show();
            //Toast.makeText(this, R.string.question_deleted, Toast.LENGTH_SHORT).show();
        }
    }

    private void showQuestion(int questionIndex) {

        // Show question at the given index
        if (mQuestionList.size() > 0) {
            if (questionIndex < 0) {
                questionIndex = mQuestionList.size() - 1;
            }
            else if (questionIndex >= mQuestionList.size()) {
                questionIndex = 0;
            }

            mCurrentQuestionIndex = questionIndex;
            updateAppBarTitle();

            Question question = mQuestionList.get(mCurrentQuestionIndex);
            mQuestionText.setText(question.getText());
            mAnswerText.setText(question.getAnswer());
        }
        else {
            // No questions yet
            mCurrentQuestionIndex = -1;
        }
    }

    private void toggleAnswerVisibility() {
        if (mAnswerText.getVisibility() == View.VISIBLE) {
            mAnswerButton.setText(R.string.show_answer);
            mAnswerText.setVisibility(View.INVISIBLE);
            mAnswerLabel.setVisibility(View.INVISIBLE);
        }
        else {
            mAnswerButton.setText(R.string.hide_answer);
            mAnswerText.setVisibility(View.VISIBLE);
            mAnswerLabel.setVisibility(View.VISIBLE);
        }
    }
}