# Specification: Product UoM Conversion CRUD

## Overview
This track implements a complete CRUD (Create, Read, Update, Delete) interface for managing `ProductUoMConversion` master data. Currently, the underlying service exists, but the user interface is missing, preventing users from defining conversion factors (e.g., from Box to Pieces) for products. This feature will be built as a separate standalone menu within the Inventory module.

## Functional Requirements
1. **Menu & Navigation**:
   - Create a new standalone menu item "UoM Conversions" in the sidebar (Inventory module).
2. **List View**:
   - Display a paginated table showing existing conversions.
   - Columns: Product Name/Code, From UoM, To UoM (Base UoM), Conversion Factor.
   - Search functionality by Product Name/Code.
3. **Form (Create / Update)**:
   - Provide a form to select a `Product` (using TomSelect autocomplete).
   - Provide a dropdown/autocomplete to select the `From UoM`.
   - Read-only display of `To UoM` (automatically fetched from the Product's Base UoM).
   - Provide a numeric input for the `Conversion Factor`.
4. **Delete**:
   - Allow users to delete a conversion record.

## Validation Rules (Strictly Enforced)
- **Prevent Duplicates**: A product cannot have two conversion records with the exact same `From UoM`.
- **Prevent Self-Conversion**: The `From UoM` selected cannot be the same as the product's `To UoM` (Base UoM).
- **Positive Factor Only**: The conversion factor must be a strictly positive number (> 0).

## Access Control & Security
- **Dedicated Permissions**: Create a specific set of permissions: `UOM-CONVERSION_READ`, `UOM-CONVERSION_CREATE`, `UOM-CONVERSION_UPDATE`, `UOM-CONVERSION_DELETE`.
- Add a Flyway migration script to seed these permissions.