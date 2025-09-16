// Specifies the package where this AddPlantFragment class resides.
// Ensure this matches your project's structure for proper compilation and access.
package com.example.myapplication.ui.myplants;

// Android framework imports for core functionalities.
import android.os.Bundle; // For passing data between components and saving instance state.
import android.view.LayoutInflater; // For instantiating layout XML files into View objects.
import android.view.View; // Base class for widgets, used to create interactive UI components.
import android.view.ViewGroup; // Base class for layouts, containers that hold other Views or ViewGroups.

// AndroidX (Jetpack) library imports for modern Android development.
import androidx.annotation.NonNull; // Annotation indicating a parameter, field, or method return value can never be null.
import androidx.annotation.Nullable; // Annotation indicating a parameter, field, or method return value can be null.
import androidx.fragment.app.Fragment; // Base class for managing a piece of an application's UI or behavior.
// Import for AppCompatActivity if you plan to interact with the ActionBar/Toolbar.
// import androidx.appcompat.app.AppCompatActivity;

// View Binding class generated from the addplant.xml layout file.
// The name of this binding class (AddplantBinding) should exactly match the XML file name
// (addplant.xml) converted to PascalCase with "Binding" appended.
import com.example.myapplication.databinding.AddplantBinding;

/**
 * AddPlantFragment provides the UI for users to add a new plant to their collection.
 * This fragment will typically contain input fields for plant details such as name,
 * scientific name, description, an option to add an image (perhaps by navigating to
 * a capture or gallery screen), and a save button.
 *
 * This is currently a skeleton fragment with TODOs indicating where to implement
 * the specific logic for view setup and user interactions.
 */
public class AddPlantFragment extends Fragment {

    // TAG for logging, useful for debugging. Optional, but good practice.
    // private static final String TAG = "AddPlantFragment";

    // View Binding instance for the addplant.xml layout.
    // This allows for type-safe access to views defined in the layout.
    private AddplantBinding binding;

    /**
     * Factory method to create a new instance of this fragment.
     * It's a good practice to use a factory method like this, especially if you
     * need to pass arguments to the fragment in the future.
     *
     * @return A new instance of fragment AddPlantFragment.
     */
    public static AddPlantFragment newInstance() {
        // You can add argument passing here if needed in the future:
        // AddPlantFragment fragment = new AddPlantFragment();
        // Bundle args = new Bundle();
        // args.putString(ARG_PARAM1, param1);
        // fragment.setArguments(args);
        // return fragment;
        return new AddPlantFragment();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is where the layout for the fragment is inflated using View Binding.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the generated ViewBinding class.
        binding = AddplantBinding.inflate(inflater, container, false);

        // TODO: Setup initial states or properties of views from addplant.xml using the binding object.
        // For example, if you have an EditText for the plant name:
        // binding.editTextPlantName.setHint("Enter plant name");
        // Or if you have a Button to save the plant:
        // binding.buttonSavePlant.setText("Save Plant");

        // Return the root view of the inflated layout.
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This is where you should set up listeners for UI elements, load initial data
     * into views, or perform other view-related initializations.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: Set up listeners and logic for your Add Plant screen.
        // This is where you would handle user interactions with the input fields and buttons.

        // Example: Set up a click listener for a save button.
        // Make sure you have a button with the ID 'buttonSavePlant' in your addplant.xml layout.
        /*
        if (binding.buttonSavePlant != null) {
            binding.buttonSavePlant.setOnClickListener(v -> {
                // 1. Get data from input fields using the binding object:
                // String plantName = binding.editTextPlantName.getText().toString().trim();
                // String scientificName = binding.editTextScientificName.getText().toString().trim();
                // ... other fields ...

                // 2. Validate the input data (e.g., check for empty fields).
                // if (plantName.isEmpty()) {
                //     binding.editTextPlantName.setError("Plant name is required");
                //     return; // Stop further processing if validation fails.
                // }

                // 3. Create a new Plant object (assuming you have a Plant model class).
                // Plant newPlant = new Plant(plantName, scientificName, ...);

                // 4. Save the new plant (e.g., to a database, ViewModel, or other data source).
                // Log.d(TAG, "Saving new plant: " + newPlant.getName());
                // savePlantToRepository(newPlant); // Example method call

                // 5. Navigate back to the previous screen or show a success message.
                // NavController navController = Navigation.findNavController(v);
                // navController.popBackStack();
                // Toast.makeText(getContext(), "Plant added successfully!", Toast.LENGTH_SHORT).show();
            });
        }
        */

        // Example: Set a toolbar title if this fragment is hosted by an AppCompatActivity
        // and you have a Toolbar/ActionBar.
        /*
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Add New Plant"); // Or use a string resource
            }
        }
        */
        // Or, if using a Toolbar view directly in your fragment's layout (e.g., with ID 'toolbarAddPlant'):
        /*
        if (binding.toolbarAddPlant != null) {
            if (getActivity() instanceof AppCompatActivity) {
                ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbarAddPlant);
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle("Add New Plant");
                    // Enable up button if you want to navigate back via toolbar
                    // actionBar.setDisplayHomeAsUpEnabled(true);
                    // actionBar.setDisplayShowHomeEnabled(true);
                }
            }
            // If not using AppCompatActivity's action bar, you can set title directly:
            // binding.toolbarAddPlant.setTitle("Add New Plant");
            // binding.toolbarAddPlant.setNavigationOnClickListener(v -> {
            //    // Handle navigation icon click, e.g., popBackStack()
            //    NavController navController = Navigation.findNavController(view);
            //    navController.popBackStack();
            // });
        }
        */
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been detached from the fragment.
     * The next time the fragment needs to be displayed, a new view will be created.
     * This is a crucial lifecycle method for cleaning up resources associated with the view,
     * especially to prevent memory leaks by nullifying the ViewBinding instance.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify the binding object when the view is destroyed.
        // This is important for ViewBinding with Fragments to prevent memory leaks
        // by releasing the reference to the view hierarchy, allowing it to be garbage collected.
        binding = null;
        // Log.d(TAG, "onDestroyView: Binding set to null.");
    }
}
