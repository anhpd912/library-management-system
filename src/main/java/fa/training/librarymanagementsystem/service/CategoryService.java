package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.request.CreateCategoryRequest;
import fa.training.librarymanagementsystem.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CreateCategoryRequest request);
    void deleteCategory(Long id);
}