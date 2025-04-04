// Image preview function
function previewImage(event) {
    var reader = new FileReader();
    reader.onload = function () {
        var output = document.getElementById('newImagePreview');
        if (output) {
            output.src = reader.result;
            output.style.display = 'block';
        }
    };
    if (event.target.files[0]) {
        reader.readAsDataURL(event.target.files[0]);
    }
}

// Label management functions
let selectedLabels = [];

// Initialize labels from the server on page load
document.addEventListener('DOMContentLoaded', function () {
    // For label editing (admin form or product add/edit)
    const labelInputs = document.querySelectorAll('#labelInputsContainer input[name="label"]');
    selectedLabels = Array.from(labelInputs).map(input => input.value);
    updateLabelsDisplay();

    // For product display page (dynamic label icons)
    const labelItems = document.querySelectorAll('#productLabelList li');
    labelItems.forEach(li => {
        const label = li.getAttribute('data-label');
        const iconClass = getLabelIconClass(label);
        const icon = document.createElement('i');
        icon.className = `fas ${iconClass}`;
        icon.title = label;
        li.appendChild(icon);
    });

    // Enable Enter key to add label if the input exists
    const newLabelInput = document.getElementById('newLabelInput');
    if (newLabelInput) {
        newLabelInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                addLabel();
            }
        });
    }
});

// Add a new label from select or input
function addLabel() {
    const select = document.getElementById('labelSelect');
    const label = select.value.trim();

    if (label && !selectedLabels.includes(label)) {
        selectedLabels.push(label);
        updateLabelsDisplay();
        select.value = ""; // Reset dropdown to placeholder
    }
}

// Remove a label
function removeLabel(label) {
    selectedLabels = selectedLabels.filter(l => l !== label);
    updateLabelsDisplay();
}

// Clear all labels
function clearLabels() {
    selectedLabels = [];
    updateLabelsDisplay();
}

// Get icon class based on label content
function getLabelIconClass(label) {
    const lowerLabel = label.toLowerCase();
    if (lowerLabel.includes('eco')) return 'fa-leaf';
    if (lowerLabel.includes('cruelty')) return 'fa-shield-dog';
    if (lowerLabel.includes('natural')) return 'fa-tree';
    if (lowerLabel.includes('vegan')) return 'fa-seedling';
    return 'fa-tag';
}

// Update the label display for both use cases (editing + viewing)
function updateLabelsDisplay() {
    const container = document.getElementById('selectedLabelsContainer');
    const inputsContainer = document.getElementById('labelInputsContainer');
    const hiddenInput = document.getElementById('labelsInput');

    if (!container) return;

    container.innerHTML = '';
    if (inputsContainer) inputsContainer.innerHTML = '';

    selectedLabels.forEach(label => {
        const labelElement = document.createElement('span');
        labelElement.className = 'selected-label';
        const iconClass = getLabelIconClass(label);

        labelElement.innerHTML = `
            <span>${label}</span>
            <i class="fas ${iconClass}"></i>
            <button type="button" onclick="removeLabel('${label}')">
                <i class="fas fa-times"></i>
            </button>
        `;
        container.appendChild(labelElement);

        if (inputsContainer) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'label';
            input.value = label;
            inputsContainer.appendChild(input);
        }
    });

    if (hiddenInput) {
        hiddenInput.value = selectedLabels.join(';');
    }
}
