service UfsService {
    string GetWeightedCategories(1:string uid, 2:string version),
    string GetWeightedTags(1:string uid, 2:string version),
}
