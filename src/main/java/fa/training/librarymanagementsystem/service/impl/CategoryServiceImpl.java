package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.request.CreateCategoryRequest;
import fa.training.librarymanagementsystem.dto.response.CategoryResponse;
import fa.training.librarymanagementsystem.entity.Book;
import fa.training.librarymanagementsystem.entity.Category;
import fa.training.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.repository.BookRepository;
import fa.training.librarymanagementsystem.repository.CategoryRepository;
import fa.training.librarymanagementsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return CategoryResponse.from(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ResourceAlreadyExistsException("Category name already taken: " + request.getName());
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        // Remove from all books' categories list so the join table rows are cleaned up
        List<Book> books = bookRepository.findByCategoriesId(id);
        books.forEach(b -> b.getCategories().remove(category));
        bookRepository.saveAll(books);
        categoryRepository.delete(category);
    }
}